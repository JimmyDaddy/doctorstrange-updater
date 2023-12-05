package com.sxc.doctorstrangeupdater;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.ReadableType;
import com.facebook.react.bridge.WritableMap;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import static com.sxc.doctorstrangeupdater.DoctorStrangeUpdaterConstants.FAILED_TO_COPY_ASSETS;
import static java.sql.Types.INTEGER;
import static java.sql.Types.NULL;

/**
 * Created by jimmydaddy on 2017/4/18.
 */

public class DoctorStrangeUpdater {

    private ReactApplicationContext context;

    public DoctorStrangeUpdater(ReactApplicationContext context){
        this.context = context;
    }

    /*******************************
     * get app info method
     */

    /**
     * get version code
     * @return version code
     */
    public String getAppVersionCodeStr(){
        PackageManager manager = this.context.getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(this.context.getPackageName(), 0);
            if (info != null && info.versionCode != NULL){
                return String.valueOf(info.versionCode);
            } else {
                return DoctorStrangeUpdaterConstants.VERSION_CODE_ERROR;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(DoctorStrangeUpdaterConstants.TAG, "getAppVersionCodeStr: "+DoctorStrangeUpdaterConstants.CAN_NOT_FOUND_PACKAGE_NAME);
            return DoctorStrangeUpdaterConstants.CAN_NOT_FOUND_PACKAGE_NAME;
        }
    }

    /**
     *
     * @return
     */
    public String getAppVersionName(){

        PackageManager manager = context.getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            return info.versionName;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(DoctorStrangeUpdaterConstants.TAG, "getSystemVersion: "+DoctorStrangeUpdaterConstants.CAN_NOT_FOUND_PACKAGE_NAME);
            return null;
        }

    }

    /**
     * get pakckage name
     * @return
     */
    public String getPackageName(){

        PackageManager manager = context.getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            return info.packageName;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(DoctorStrangeUpdaterConstants.TAG, "getSystemVersion: "+DoctorStrangeUpdaterConstants.CAN_NOT_FOUND_PACKAGE_NAME);
            return null;
        }
    }

    /***********************************
     * sharedpreferences op method
     */

    /**
     * init first open of native app version
     */
    public void initAppVersionFirstOpen(){
        String versionCode = this.getAppVersionCodeStr();
        String flag = DoctorStrangeUpdaterConstants.APP_VERSION_FIRST_OPEN + "_" + versionCode;
        SharedPreferences preferences = this.context.getSharedPreferences(DoctorStrangeUpdaterConstants.DOCTOR_STRANGE_UPDATER_PREFERENCEKEY, Context.MODE_PRIVATE);
        //如果是第一次打开
        if (!preferences.contains(flag)){
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(flag, true);
            editor.commit();
        }
    }

    /**
     *
     * @return whether the app of this native version first open
     */
    public boolean getFirstOpenOfAppversion(){
        String versionCode = this.getAppVersionCodeStr();
        String flag = DoctorStrangeUpdaterConstants.APP_VERSION_FIRST_OPEN + "_" + versionCode;
        SharedPreferences preferences = this.context.getSharedPreferences(DoctorStrangeUpdaterConstants.DOCTOR_STRANGE_UPDATER_PREFERENCEKEY, Context.MODE_PRIVATE);
        //如果是第一次打开
        if (!preferences.contains(flag) || preferences.getBoolean(flag, false)){
            return true;
        } else {
            return false;
        }
    }

    /**
     * set false for first open
     */
    public void afterFirstOpenInit(){

        String versionCode = this.getAppVersionCodeStr();
        String flag = DoctorStrangeUpdaterConstants.APP_VERSION_FIRST_OPEN + "_" + versionCode;
        SharedPreferences preferences = this.context.getSharedPreferences(DoctorStrangeUpdaterConstants.DOCTOR_STRANGE_UPDATER_PREFERENCEKEY, Context.MODE_PRIVATE);
        //如果是第一次打开
        if (preferences.contains(flag)){
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(flag, false);
            editor.commit();
        }
    }

    /**
     *
     * @param jsonObject
     * @param preferenceName
     * @throws JSONException
     */
    public void setMetaDataByJson(JSONObject jsonObject, String preferenceName) throws JSONException{
        Iterator<String> iterator = jsonObject.keys();

        SharedPreferences preferences = this.context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        while(iterator.hasNext()){
            String key = iterator.next();

            Object value = jsonObject.get(key);

            String className = value.getClass().getSimpleName();

            switch (className){
                case "String": editor.putString(key, String.valueOf(value.toString()));
                    break;
                case "Boolean": editor.putBoolean(key, Boolean.valueOf(value.toString()));
                    break;
                case "Integer": editor.putInt(key, Integer.valueOf(value.toString()));
                    break;
            }
        }

        editor.commit();
    }

    /**
     * get metadata by key
     * @param key
     * @return
     */
    public WritableMap getMetaDataByKey(String key){
        WritableMap params = Arguments.createMap();

        SharedPreferences sharedPreferences = context.getSharedPreferences(key, Context.MODE_PRIVATE);

        Map<String, ?> map = sharedPreferences.getAll();

        if (!map.isEmpty()){
            for (String mapKey: map.keySet()) {
                Object value = map.get(mapKey);
                if (value != null){
                    String className = value.getClass().getSimpleName();

                    switch (className) {
                        case "String": params.putString(mapKey, String.valueOf(map.get(mapKey))); break;
                        case "Boolean": params.putBoolean(mapKey, Boolean.valueOf(map.get(mapKey).toString()));break;
                        case "Integer": params.putInt(mapKey, Integer.valueOf(map.get(mapKey).toString()));break;
                    }

                } else {
                    params.putString(mapKey, null);
                }
            }
        }

        return params;
    }

    /**
     * set metadata by js object
     * @param params
     * @param key
     */
    public void setMetaDataByReadableMap(ReadableMap params, String key){
        ReadableMapKeySetIterator it = params.keySetIterator();
        SharedPreferences sharedPreferences = context.getSharedPreferences(key, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        while (it.hasNextKey()){
            String mKey = it.nextKey();
            ReadableType type = params.getType(mKey);

            switch (type) {
                case Number: editor.putInt(mKey, params.getInt(mKey));break;
                case Boolean: editor.putBoolean(mKey, params.getBoolean(mKey));break;
                case String: editor.putString(mKey, params.getString(mKey));break;
                default: editor.putString(mKey, null);
            }
        }

        editor.commit();
    }

    /*************************************
     * file op method
     */

    /**
     * create folder
     * @param path
     * @return path or null(if failed)
     */
    public String createFolderIfNotExist(String path){
        try{
            File folder = new File(path);
            if (folder.exists()){
                return folder.getAbsolutePath();
            } else {
                folder.mkdir();
                if (folder.exists()){
                    return folder.getAbsolutePath();
                } else {
                    Log.d(DoctorStrangeUpdaterConstants.TAG, "createFolderIfNotExist: " + DoctorStrangeUpdaterConstants.FOLDER_CAN_NOT_BE_CREATED+folder.getName());
                    throw new Exception(DoctorStrangeUpdaterConstants.FOLDER_CAN_NOT_BE_CREATED+folder.getName());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(DoctorStrangeUpdaterConstants.TAG, "createFolderIfNotExist: "+e.getMessage());
            return null;
        }

    }

    public static void recursionDeleteFile(File file) {
        if (file.isFile()) {
            file.delete();
            return;
        }
        if (file.isDirectory()) {
            File[] childFile = file.listFiles();
            if (childFile == null || childFile.length == 0) {
                file.delete();
                return;
            }
            for (File f : childFile) {
                recursionDeleteFile(f);
            }
            file.delete();
        }
    }


    /**
     * remove file if exist
     * @param path file path
     * @return op success
     */
    public boolean removeFileIfExist(String path){
        try{
            File file = new File(path);
            if (file.exists()){
                recursionDeleteFile(file);
                if (file.exists()){
                    Log.e(DoctorStrangeUpdaterConstants.TAG, "removeFileIfExist: " + DoctorStrangeUpdaterConstants.FAILED_TO_REMOVE_FOLDER+file.getName());
                    throw new Exception(DoctorStrangeUpdaterConstants.FAILED_TO_REMOVE_FOLDER+file.getName());
                } else {
                    return true;
                }
            } else {
                return true;
            }
        } catch (Exception e){
            e.printStackTrace();
            Log.e(DoctorStrangeUpdaterConstants.TAG, "removeFileIfExist: " + e.getMessage());
            return false;
        }
    }

    /**
     *
     * @param assetName
     * @param destination
     * @return
     * @throws IOException
     * @throws Exception
     */
    public File copyAssets(String assetName, String destination) throws IOException, Exception {

        try {

            AssetManager assetManager = this.context.getAssets();
            //获取到zip输入流
            InputStream is = assetManager.open(assetName);
            FileOutputStream fos = new FileOutputStream(destination);
            byte[] buffer = new byte[1024];
            int byteCount=0;
            while((byteCount=is.read(buffer))!=-1) {//循环从输入流读取 buffer字节
                fos.write(buffer, 0, byteCount);//将读取的输入流写入到输出流
            }
            fos.flush();//刷新缓冲区
            is.close();
            fos.close();

            File zipFile = new File(destination);

            if (zipFile.exists()){
                return zipFile;
            } else {
                throw new Exception(FAILED_TO_COPY_ASSETS);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(DoctorStrangeUpdaterConstants.TAG, "copyAssets: "+e.getMessage());
            throw e;
        }
    }

    /**
     *
     * @param file
     * @param destination
     * @throws ZipException
     * @throws IOException
     */
    public void unZipFile(File file, String destination) throws ZipException, IOException{
        try {
            ZipFile zipFile = new ZipFile(file);
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
        } catch (Exception e) {
            Log.e(DoctorStrangeUpdaterConstants.TAG, "unZipFile: " + e.getMessage());
            e.printStackTrace();
            throw  e;
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

    /**
     *
     * @param metaDataName
     * @return
     * @throws IOException
     */
    public String getStringFromAssetMetaData(String metaDataName) throws IOException{
        AssetManager assetManager = this.context.getAssets();
        String jsonString = null;
        InputStream inputStream = assetManager.open(metaDataName);
        int size = inputStream.available();
        byte[] buffer = new byte[size];
        inputStream.read(buffer);
        inputStream.close();
        jsonString = new String(buffer, "UTF-8");
        return jsonString;

    }

    /**
     * back version
     * @throws IOException
     */
    public void backUpVersion() throws Exception {

        String backupSourceRoot = context.getDir("Library", Context.MODE_PRIVATE) + "/" + DoctorStrangeUpdaterConstants.PRE_ROOT;
        removeFileIfExist(backupSourceRoot);

        File backUpFolder = new File(backupSourceRoot);

        backUpFolder.mkdir();

        if (backUpFolder.exists()){
            String sourceRoot = context.getDir("Library", Context.MODE_PRIVATE) + "/" + DoctorStrangeUpdaterConstants.SOURCE_ROOT;
            String zipFileAtSourceRoot = sourceRoot+"/doctor.zip";
            String zipFileAtPreRoot = backupSourceRoot + "/doctor.zip";
            copyFileToDestination(zipFileAtSourceRoot, zipFileAtPreRoot);
            File zipFile = new File(zipFileAtPreRoot);

            if (zipFile.exists()){
                unZipFile(zipFile, backupSourceRoot);
            }
        } else {
            Log.e(DoctorStrangeUpdaterConstants.TAG, "backUpVersion: "+ DoctorStrangeUpdaterConstants.BACKUP_FOLDER_CREATE_FAILED );
            throw new Exception(DoctorStrangeUpdaterConstants.BACKUP_FOLDER_CREATE_FAILED);
        }
    }

    /**
     * back to pre version
     * @return whether success
     * @throws IOException
     */
    public void backToPreVersion() throws IOException, Exception{
        //删除操作不可逆，先确定备份文件一定存在
        String backupSourceRoot = context.getDir("Library", Context.MODE_PRIVATE) + "/" + DoctorStrangeUpdaterConstants.PRE_ROOT;
        String backupAssets = backupSourceRoot + "/" + DoctorStrangeUpdaterConstants.ASSETS_FOLDER;
        String backupBundle = backupSourceRoot + "/" + DoctorStrangeUpdaterConstants.BUNDLE_NAME;

        if (fileExist(backupSourceRoot) && fileExist(backupAssets) && fileExist(backupBundle)) {
            //删除当前资源文件
            String sourceRoot = context.getDir("Library", Context.MODE_PRIVATE) + "/" + DoctorStrangeUpdaterConstants.SOURCE_ROOT;

            if(removeFileIfExist(sourceRoot)){
                File backupSourceRootFolder = new File(backupSourceRoot);
                //重命名资源文件夹
                if(backupSourceRootFolder.renameTo(new File(sourceRoot))) {
                    //设置新的当前版本信息
                    SharedPreferences prePrefs = context.getSharedPreferences(DoctorStrangeUpdaterConstants.PREVIOUS_JS_CODE_METADATA, Context.MODE_PRIVATE);

                    if(prePrefs.contains(DoctorStrangeUpdaterConstants.VERSION)){
                        SharedPreferences curPrefs = context.getSharedPreferences(DoctorStrangeUpdaterConstants.CURRENT_JS_CODE_METADATA, Context.MODE_PRIVATE);

                        String rollBackVersion = curPrefs.getString(DoctorStrangeUpdaterConstants.VERSION, null);

                        if (rollBackVersion != null) {
                            //设置已回滚版本号
                            SharedPreferences lastRollBack = context.getSharedPreferences(DoctorStrangeUpdaterConstants.JS_LAST_ROLLBACK_VERSION, Context.MODE_PRIVATE);
                            lastRollBack.edit().putString(DoctorStrangeUpdaterConstants.VERSION, rollBackVersion).apply();

                            //设置当前版本信息为前一版本信息
                            Map<String, ?> map = prePrefs.getAll();

                            SharedPreferences.Editor curEditor = curPrefs.edit();

                            if (!map.isEmpty()){
                                for (String mapKey: map.keySet()) {
                                    Object value = map.get(mapKey);
                                    if (value != null){
                                        String className = value.getClass().getSimpleName();

                                        switch (className) {
                                            case "String": curEditor.putString(mapKey, String.valueOf(map.get(mapKey))); break;
                                            case "Boolean": curEditor.putBoolean(mapKey, Boolean.valueOf(map.get(mapKey).toString()));break;
                                            case "Integer": curEditor.putInt(mapKey, Integer.valueOf(map.get(mapKey).toString()));break;
                                        }

                                    } else {
                                        curEditor.putString(mapKey, null);
                                    }
                                }
                            }
                            curEditor.apply();

                            //清空上一版本信息
                            SharedPreferences.Editor preEditor = prePrefs.edit();
                            preEditor.clear().apply();

                            return;
                        }
                    }
                }
            }
        }

        throw new Exception(DoctorStrangeUpdaterConstants.FAILED_TO_BACK_PRE);

    }

    public boolean fileExist(String fileName){
        File file = new File(fileName);
        return file.exists();
    }

    public void copyFileToDestination(String origin, String Deatination) throws IOException{

        InputStream in = new FileInputStream(origin);
        OutputStream out = new FileOutputStream(Deatination);

        byte[] buffer = new byte[1024];
        int length;
        while ((length = in.read(buffer)) > 0) {
            out.write(buffer, 0, length);
        }
        in.close();
        out.close();
    }


    /****************
     * native method
     *
     */

    /**
     * @param oldfile
     * @param newfile
     * @param patchfile
     * @return
     */


    static {
        System.loadLibrary("doctorstrange");
    }

    public native static int beginPatch(String oldfile, String newfile, String patchfile);

}
