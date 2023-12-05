package com.sxc.doctorstrangeupdater;

import android.content.Context;
import android.os.Environment;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;
import java.util.HashMap;

import android.os.StatFs;
import android.util.Base64;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.modules.core.RCTNativeAppEventEmitter;


/**
 * Created by jimmydaddy on 2017/4/11.
 */

public class DoctorFSManager extends ReactContextBaseJavaModule {

        private static final String DocumentDirectoryPath = "DocumentDirectoryPath";
        private static final String ExternalDirectoryPath = "ExternalDirectoryPath";
        private static final String ExternalStorageDirectoryPath = "ExternalStorageDirectoryPath";
        private static final String PicturesDirectoryPath = "PicturesDirectoryPath";
        private static final String TemporaryDirectoryPath = "TemporaryDirectoryPath";
        private static final String CachesDirectoryPath = "CachesDirectoryPath";
        private static final String DocumentDirectory = "DocumentDirectory";
        private static final String LibraryDirectoryPath = "LibraryDirectoryPath";

        private Downloader downloader = new Downloader();

        public DoctorFSManager(ReactApplicationContext reactContext) {
            super(reactContext);
        }

        @Override
        public String getName() {
            return "DoctorFSManager";
        }

        @ReactMethod
        public void writeFile(String filepath, String base64Content, Promise promise) {
            try {
                byte[] bytes = Base64.decode(base64Content, Base64.DEFAULT);

                FileOutputStream outputStream = new FileOutputStream(filepath, false);
                outputStream.write(bytes);
                outputStream.close();

                promise.resolve(null);
            } catch (Exception ex) {
                ex.printStackTrace();
                reject(promise, filepath, ex);
            }
        }

        @ReactMethod
        public void exists(String filepath, Promise promise) {
            try {
                File file = new File(filepath);
                promise.resolve(file.exists());
            } catch (Exception ex) {
                ex.printStackTrace();
                reject(promise, filepath, ex);
            }
        }

        @ReactMethod
        public void readFile(String filepath, Promise promise) {
            try {
                File file = new File(filepath);

                if (file.isDirectory()) {
                    rejectFileIsDirectory(promise);
                    return;
                }

                if (!file.exists()) {
                    rejectFileNotFound(promise, filepath);
                    return;
                }

                FileInputStream inputStream = new FileInputStream(filepath);
                byte[] buffer = new byte[(int)file.length()];
                inputStream.read(buffer);

                String base64Content = Base64.encodeToString(buffer, Base64.NO_WRAP);

                promise.resolve(base64Content);
            } catch (Exception ex) {
                ex.printStackTrace();
                reject(promise, filepath, ex);
            }
        }

        @ReactMethod
        public void moveFile(String filepath, String destPath, Promise promise) {
            try {
                File inFile = new File(filepath);

                if (!inFile.renameTo(new File(destPath))) {
                    copyFile(filepath, destPath);

                    inFile.delete();
                }

                promise.resolve(true);
            } catch (Exception ex) {
                ex.printStackTrace();
                reject(promise, filepath, ex);
            }
        }

        @ReactMethod
        public void copyFile(String filepath, String destPath, Promise promise) {
            try {
                copyFile(filepath, destPath);

                promise.resolve(null);
            } catch (Exception ex) {
                ex.printStackTrace();
                reject(promise, filepath, ex);
            }
        }

        private void copyFile(String filepath, String destPath) throws IOException {
            InputStream in = new FileInputStream(filepath);
            OutputStream out = new FileOutputStream(destPath);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
            in.close();
            out.close();
        }


        @ReactMethod
        public void unlink(String filepath, Promise promise) {
            try {
                File file = new File(filepath);

                if (!file.exists()) throw new Exception("File does not exist");

                DeleteRecursive(file);

                promise.resolve(null);
            } catch (Exception ex) {
                ex.printStackTrace();
                reject(promise, filepath, ex);
            }
        }

        private void DeleteRecursive(File fileOrDirectory) {
            if (fileOrDirectory.isDirectory()) {
                for (File child : fileOrDirectory.listFiles()) {
                    DeleteRecursive(child);
                }
            }

            fileOrDirectory.delete();
        }

        @ReactMethod
        public void mkdir(String filepath, ReadableMap options, Promise promise) {
            try {
                File file = new File(filepath);

                file.mkdirs();

                boolean exists = file.exists();

                if (!exists) throw new Exception("Directory could not be created");

                promise.resolve(null);
            } catch (Exception ex) {
                ex.printStackTrace();
                reject(promise, filepath, ex);
            }
        }

        private void sendEvent(ReactContext reactContext, String eventName, @Nullable WritableMap params) {
            reactContext
                    .getJSModule(RCTNativeAppEventEmitter.class)
                    .emit(eventName, params);
        }

        @ReactMethod
        public void downloadFile(final ReadableMap options, final Promise promise) {
            try {
                File file = new File(options.getString("toFile"));
                URL url = new URL(options.getString("fromUrl"));
                ReadableMap headers = options.getMap("headers");
                int progressDivider = options.getInt("progressDivider");

                DownloadParams params = new DownloadParams();

                params.src = url;
                params.dest = file;
                params.headers = headers;
                params.progressDivider = progressDivider;

                params.onTaskCompleted = new DownloadParams.OnTaskCompleted() {
                    public void onTaskCompleted(DownloadResult res) {
                        if (res.exception == null) {
                            WritableMap infoMap = Arguments.createMap();

                            infoMap.putInt("statusCode", res.statusCode);
                            infoMap.putInt("bytesWritten", res.bytesWritten);

                            promise.resolve(infoMap);
                        } else {
                            reject(promise, options.getString("toFile"), res.exception);
                        }
                    }
                };

                params.onDownloadBegin = new DownloadParams.OnDownloadBegin() {
                    public void onDownloadBegin(int statusCode, int contentLength, Map<String, String> headers) {
                        WritableMap headersMap = Arguments.createMap();

                        for (Map.Entry<String, String> entry : headers.entrySet()) {
                            headersMap.putString(entry.getKey(), entry.getValue());
                        }

                        WritableMap data = Arguments.createMap();

                        data.putInt("statusCode", statusCode);
                        data.putInt("contentLength", contentLength);
                        data.putMap("headers", headersMap);

                        sendEvent(getReactApplicationContext(), "DownloadBegin-", data);
                    }
                };

                params.onDownloadProgress = new DownloadParams.OnDownloadProgress() {
                    public void onDownloadProgress(int contentLength, int bytesWritten) {
                        WritableMap data = Arguments.createMap();

                        data.putInt("contentLength", contentLength);
                        data.putInt("bytesWritten", bytesWritten);

                        sendEvent(getReactApplicationContext(), "DownloadProgress-", data);
                    }
                };


                this.downloader.execute(params);

            } catch (Exception ex) {
                ex.printStackTrace();
                reject(promise, options.getString("toFile"), ex);
            }
        }

        @ReactMethod
        public void stopDownload() {
                downloader.stop();
        }

        @ReactMethod
        public void uzipFileAtPath(String origin, String destination, Promise promise){
            try {
                ZipFile zipFile = new ZipFile(origin);
                Enumeration zList=zipFile.entries();

                ZipEntry ze;
                byte[] buf=new byte[1024];
                while(zList.hasMoreElements()){
                    ze=(ZipEntry)zList.nextElement();
                    if(ze.isDirectory()){
                        String dirstr = destination +"/" + ze.getName();
                        File f=new File(dirstr);
                        f.mkdir();
                        continue;
                    }
                    OutputStream os=new BufferedOutputStream(new FileOutputStream(getRealFileName(destination, ze.getName())));
                    InputStream is=new BufferedInputStream(zipFile.getInputStream(ze));
                    int readLen=0;
                    while ((readLen=is.read(buf, 0, 1024))!=-1) {
                        os.write(buf, 0, readLen);
                    }
                    is.close();
                    os.close();
                }
                zipFile.close();
                File file = new File(destination+"/"+DoctorStrangeUpdaterConstants.BUNDLE_NAME);
                if (file.exists()){
                    promise.resolve(file.getAbsolutePath());
                } else {
                    promise.reject(DoctorStrangeUpdaterConstants.TAG, DoctorStrangeUpdaterConstants.CAN_NOT_FOUND_BUNDLE);
                }
            } catch (Exception e) {
                Log.e(DoctorStrangeUpdaterConstants.TAG, "unZipFile: " + e.getMessage());
                e.printStackTrace();
                promise.reject(DoctorStrangeUpdaterConstants.TAG , e.getMessage());
            }

        }

    /**
     * get path of file
     * @param baseDir
     * @param absFileName
     * @return
     */
    private File getRealFileName(String baseDir, String absFileName){
        String[] dirs=absFileName.split("/");
        File ret=new File(baseDir);
        String substr = null;
        Log.e(DoctorStrangeUpdaterConstants.TAG, "getRealFileName: "+dirs.length+" _ "+dirs[0]);
        if(dirs.length>=1){
            for (int i = 0; i < dirs.length-1;i++) {
                substr = dirs[i];
                ret = new File(ret, substr);
            }
            if(!ret.exists())
                ret.mkdirs();
            substr = dirs[dirs.length-1];
            ret=new File(ret, substr);
            return ret;
        } else {
            ret = new File(ret, absFileName);
            return ret;
        }
    }



    private void reject(Promise promise, String filepath, Exception ex) {
            if (ex instanceof FileNotFoundException) {
                rejectFileNotFound(promise, filepath);
                return;
            }

            promise.reject(null, ex.getMessage());
        }

        private void rejectFileNotFound(Promise promise, String filepath) {
            promise.reject("ENOENT", "ENOENT: no such file or directory, open '" + filepath + "'");
        }

        private void rejectFileIsDirectory(Promise promise) {
            promise.reject("EISDIR", "EISDIR: illegal operation on a directory, read");
        }

        @Override
        public Map<String, Object> getConstants() {
            final Map<String, Object> constants = new HashMap<>();

            constants.put(DocumentDirectory, 0);
            constants.put(DocumentDirectoryPath, this.getReactApplicationContext().getFilesDir().getAbsolutePath());
            constants.put(TemporaryDirectoryPath, null);
            constants.put(PicturesDirectoryPath, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath());
            constants.put(CachesDirectoryPath, this.getReactApplicationContext().getCacheDir().getAbsolutePath());

            File externalStorageDirectory = Environment.getExternalStorageDirectory();
            if (externalStorageDirectory != null) {
                constants.put(ExternalStorageDirectoryPath, externalStorageDirectory.getAbsolutePath());
            } else {
                constants.put(ExternalStorageDirectoryPath, null);
            }

            File externalDirectory = this.getReactApplicationContext().getExternalFilesDir(null);
            if (externalDirectory != null) {
                constants.put(ExternalDirectoryPath, externalDirectory.getAbsolutePath());
            } else {
                constants.put(ExternalDirectoryPath, null);
            }

            File libraryDirectoryPath = this.getReactApplicationContext().getDir("Library", Context.MODE_PRIVATE);
            if (libraryDirectoryPath.exists() == false){
                libraryDirectoryPath.mkdir();
            }

            constants.put(LibraryDirectoryPath, libraryDirectoryPath.getAbsolutePath());

            return constants;
        }
    }