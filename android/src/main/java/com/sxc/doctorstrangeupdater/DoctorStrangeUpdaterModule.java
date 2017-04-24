package com.sxc.doctorstrangeupdater;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.react.ReactApplication;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ExecutorToken;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.UiThreadUtil;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.cxxbridge.JSBundleLoader;

import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import static com.sxc.doctorstrangeupdater.DoctorStrangeUpdaterConstants.BUNDLE_NAME;
import static com.sxc.doctorstrangeupdater.DoctorStrangeUpdaterConstants.TAG;

public class DoctorStrangeUpdaterModule extends ReactContextBaseJavaModule {

    private ReactApplicationContext context;
    private int widowWidth;
    private DoctorStrangeUpdater doctorStrangeUpdater;
    private boolean showInfo = false;

    private static DoctorStrangeUpdaterModule mInstance;
    private String defaultMetaDataName = DoctorStrangeUpdaterConstants.DEFAULT_METADATA_NAME;


    private DoctorStrangeUpdaterModule(ReactApplicationContext context) {
        super(context);

        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        widowWidth = outMetrics.widthPixels;
        this.context = context;
        this.doctorStrangeUpdater = new DoctorStrangeUpdater(context);

    }

    public static DoctorStrangeUpdaterModule getInstance(ReactApplicationContext context, String defaultMetaDataName){
        if (mInstance == null){
            mInstance = new DoctorStrangeUpdaterModule(context);
        }
        if (defaultMetaDataName != null){
            mInstance.defaultMetaDataName = defaultMetaDataName;
        }

        mInstance.initBeforeStartUp();

        return mInstance;
    }




    /**********************8
     * js export method
     */

    @Override
    public String getName() {
        return "DoctorStrangeUpdater";
    }

    @Nullable
    @Override
    public Map<String, Object> getConstants() {
        Map<String, Object> constants = new HashMap<String, Object>();
        SharedPreferences prefs = context.getSharedPreferences(DoctorStrangeUpdaterConstants.CURRENT_JS_CODE_METADATA, Context.MODE_PRIVATE);

        WritableMap params = Arguments.createMap();

        String description =  prefs.getString(DoctorStrangeUpdaterConstants.DESCRIPTION, null);
        constants.put(DoctorStrangeUpdaterConstants.DESCRIPTION, description);
        params.putString(DoctorStrangeUpdaterConstants.DESCRIPTION, description);

        String version = prefs.getString(DoctorStrangeUpdaterConstants.VERSION, null);
        constants.put("jsCodeVersion", version);
        params.putString(DoctorStrangeUpdaterConstants.VERSION, version);

        String minConatinerVersion = prefs.getString(DoctorStrangeUpdaterConstants.MIN_CONTAINER_VERSION, null);
        constants.put(DoctorStrangeUpdaterConstants.MIN_CONTAINER_VERSION, minConatinerVersion);
        params.putString(DoctorStrangeUpdaterConstants.MIN_CONTAINER_VERSION, minConatinerVersion);

        String minContainerBuildNumer = prefs.getString(DoctorStrangeUpdaterConstants.MIN_CONTAINER_BUILD_NUMBER, null);
        constants.put(DoctorStrangeUpdaterConstants.MIN_CONTAINER_BUILD_NUMBER, minContainerBuildNumer);
        params.putString(DoctorStrangeUpdaterConstants.MIN_CONTAINER_BUILD_NUMBER, minContainerBuildNumer);

        constants.put("currentMetaDataKey", DoctorStrangeUpdaterConstants.CURRENT_JS_CODE_METADATA);
        constants.put("previousMetaDataKey", DoctorStrangeUpdaterConstants.PREVIOUS_JS_CODE_METADATA);
        constants.put("currentMetaData", params);
        constants.put("lastCheckDateKey", DoctorStrangeUpdaterConstants.LAST_CHECKUPDATE_DATE);

        constants.put("firstLoadkey", DoctorStrangeUpdaterConstants.JS_FIRST_LOAD);
        constants.put("firstLoadSuccess", DoctorStrangeUpdaterConstants.JS_FIRST_LOAD_SUCCESS);

        constants.put("appVersion", doctorStrangeUpdater.getAppVersionName());
        constants.put("buildNumber", doctorStrangeUpdater.getAppVersionCodeStr());
        constants.put("systemVersion", Build.VERSION.RELEASE);
        constants.put("model", Build.MODEL);
        constants.put("deviceId", Build.BOARD);
        constants.put("systemName", "Android");
        constants.put("bundleIdentifier", doctorStrangeUpdater.getPackageName());

        constants.put("brand", Build.BRAND);


        return constants;
    }

    /**
     * show message
     * @param msg
     * @param color
     */
    @ReactMethod
    public void showMessageOnStatusBar(String msg, String color) {
        if (color == null){
            color = DoctorStrangeUpdaterConstants.COLOR_SUCCESS;
        }
        showToast(msg, color);
    }

    /**
     * set whether show info
     * @param showInfo
     */
    @ReactMethod
    public void showInfo(boolean showInfo){
        this.showInfo = showInfo;
    }

    /**
     * patch file
     * @param patch
     * @param origin
     * @param destination
     * @param promise
     */
    @ReactMethod
    public void patch(String patch, String origin, String destination, Promise promise){
        File patchfile = new File(patch);
        if (!patchfile.exists()){
            Log.e(TAG, "patch: patch file not exist : " + patch );
            promise.reject(DoctorStrangeUpdaterConstants.PATCH_FILE_NOT_EXIST, "patch file not exist : " + patch);
        }

        File originfile = new File(origin);

        if (!originfile.exists()){
            Log.e(TAG, "patch: origin file not exist : " + origin );
            promise.reject(DoctorStrangeUpdaterConstants.PATCH_ORIGIN_NOT_EXIST, "origin file not exist : " + origin);
        }

        File destinationfile = new File(destination);

        if (destinationfile.exists()){
            destinationfile.delete();
        }

        int err = doctorStrangeUpdater.beginPatch(origin, destination, patch);
        if (err != 0) {
            Log.e(TAG, "patch: failed" );
            promise.reject(DoctorStrangeUpdaterConstants.PATCH_FAIL, "patch failed origin： " + origin + ", patch: " + patch + ", destination: "+ destination);
        } else {
            Log.e(TAG, "patch: success" );
            patchfile.delete();
            originfile.delete();
            promise.resolve(destination);
            return;
        }
    }

    /**
     * get first load params
     * @param promise
     */
    @ReactMethod
    public void getFirstLoad(Promise promise){
        WritableMap currentVersion = doctorStrangeUpdater.getMetaDataByKey(DoctorStrangeUpdaterConstants.CURRENT_JS_CODE_METADATA);
        WritableMap rollBackVersion = doctorStrangeUpdater.getMetaDataByKey(DoctorStrangeUpdaterConstants.JS_LAST_ROLLBACK_VERSION);

        WritableMap params = Arguments.createMap();

        if (currentVersion.hasKey(DoctorStrangeUpdaterConstants.JS_FIRST_LOAD)){
            params.putBoolean("firstLoad", currentVersion.getBoolean(DoctorStrangeUpdaterConstants.JS_FIRST_LOAD));
        } else {
            params.putBoolean("firstLoad", true);
        }

        if (currentVersion.hasKey(DoctorStrangeUpdaterConstants.JS_FIRST_LOAD_SUCCESS)){
            params.putBoolean("firstLoadSuccess", currentVersion.getBoolean(DoctorStrangeUpdaterConstants.JS_FIRST_LOAD_SUCCESS));
        } else {
            params.putBoolean("firstLoad", false);
        }

        if (rollBackVersion.hasKey(DoctorStrangeUpdaterConstants.VERSION)){
            params.putString("updateFailVersion", rollBackVersion.getString(DoctorStrangeUpdaterConstants.JS_LAST_ROLLBACK_VERSION));
        } else {
            params.putString("updateFailVersion", "");
        }

        promise.resolve(params);
    }

    @ReactMethod
    public void setMetaData(ReadableMap params, String key){
        doctorStrangeUpdater.setMetaDataByReadableMap(params, key);
    }

    @ReactMethod
    public void backUpVersion(Promise promise){
        try {
            doctorStrangeUpdater.backUpVersion();
            promise.resolve(DoctorStrangeUpdaterConstants.BACKUP_SUCCESS);
        } catch (Exception e) {
            Log.e(TAG, "backUpVersion: " + e.getMessage());
            promise.reject(DoctorStrangeUpdaterConstants.BACKUP_FAILED, e.getMessage());
        }
    }

    @ReactMethod
    public void reload(String bundlePath){

        final Activity currentActivity = getCurrentActivity();
        if (currentActivity == null) {
            return;
        }

        File bundleFile = new File(bundlePath);
        final String mBundlePath = bundlePath;
        if (bundleFile.exists()){
            mInstance = null;
            currentActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    try{
                        Application application = currentActivity.getApplication();
                        ReactInstanceManager instanceManager = ((ReactApplication) application).getReactNativeHost().getReactInstanceManager();


                        if (instanceManager.getClass().getSimpleName().equals("XReactInstanceManagerImpl")) {
                            JSBundleLoader loader = JSBundleLoader.createFileLoader(mBundlePath);
                            Field jsBundleField = instanceManager.getClass().getDeclaredField("mBundleLoader");
                            jsBundleField.setAccessible(true);
                            jsBundleField.set(instanceManager, loader);
                        } else {
                            Field jsBundleField = instanceManager.getClass().getDeclaredField("mJSBundleFile");
                            jsBundleField.setAccessible(true);
                            jsBundleField.set(instanceManager, mBundlePath);
                        }

                        final Method recreateMethod = instanceManager.getClass().getMethod("recreateReactContextInBackground");

                        final ReactInstanceManager finalizedInstanceManager = instanceManager;

                        recreateMethod.invoke(finalizedInstanceManager);

                        currentActivity.recreate();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e(TAG, "run: restart App : " + e.getMessage());
                    }
                }
            });

        }

    }



    @ReactMethod
    public void backToPre(){
        if (showInfo){
            showToast("当前版本有误，开始进行版本回滚", DoctorStrangeUpdaterConstants.COLOR_ERROR);
        }

        try {
            doctorStrangeUpdater.backToPreVersion();

            final Activity currentActivity = getCurrentActivity();
            if (currentActivity == null) {
                return;
            }

            final String bundlePath = context.getDir("Library", Context.MODE_PRIVATE) + "/" + DoctorStrangeUpdaterConstants.SOURCE_ROOT + "/" + BUNDLE_NAME;
            File bundleFile = new File(bundlePath);

            if (bundleFile.exists()){
                mInstance = null;
                currentActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            Application application = currentActivity.getApplication();
                            ReactInstanceManager instanceManager = ((ReactApplication) application).getReactNativeHost().getReactInstanceManager();


                            if (instanceManager.getClass().getSimpleName().equals("XReactInstanceManagerImpl")) {
                                JSBundleLoader loader = JSBundleLoader.createFileLoader(bundlePath);
                                Field jsBundleField = instanceManager.getClass().getDeclaredField("mBundleLoader");
                                jsBundleField.setAccessible(true);
                                jsBundleField.set(instanceManager, loader);
                            } else {
                                Field jsBundleField = instanceManager.getClass().getDeclaredField("mJSBundleFile");
                                jsBundleField.setAccessible(true);
                                jsBundleField.set(instanceManager, bundlePath);
                            }

                            final Method recreateMethod = instanceManager.getClass().getMethod("recreateReactContextInBackground");

                            final ReactInstanceManager finalizedInstanceManager = instanceManager;

                            recreateMethod.invoke(finalizedInstanceManager);

                            currentActivity.recreate();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.e(TAG, "run: restart App : " + e.getMessage());
                        }

                    }
                });

            } else {
                if (showInfo){
                    showToast("版本回滚失败", DoctorStrangeUpdaterConstants.COLOR_ERROR);
                }

            }

        } catch (Exception e) {
            Log.e(TAG, "backToPre: " + e.getMessage());
        }


    }


    /************************
     * private method
     */

    /**
     * init before app start
     */
    private void initBeforeStartUp(){
        //初始化是否第一次打开
        doctorStrangeUpdater.initAppVersionFirstOpen();

        if (doctorStrangeUpdater.getFirstOpenOfAppversion()){
            //如果是第一次打开，删除之前的资源
            String sourceRoot = context.getDir("Library", Context.MODE_PRIVATE) + "/" + DoctorStrangeUpdaterConstants.SOURCE_ROOT;
            if (doctorStrangeUpdater.removeFileIfExist(sourceRoot)){
                try{

                    //移动当前资源到 library下
                    File rootDir = new File(sourceRoot);
                    rootDir.mkdir();
                    String zipFileAtSourceRoot = sourceRoot+"/doctor.zip";
                    File zipFile = doctorStrangeUpdater.copyAssets(DoctorStrangeUpdaterConstants.ZIP_FILE, zipFileAtSourceRoot);

                    //解压文件
                    doctorStrangeUpdater.unZipFile(zipFile, sourceRoot);

                    //设置当前版本信息
                    JSONObject json = new JSONObject(doctorStrangeUpdater.getStringFromAssetMetaData(this.defaultMetaDataName));
                    doctorStrangeUpdater.setMetaDataByJson(json, DoctorStrangeUpdaterConstants.CURRENT_JS_CODE_METADATA);
                    //设置app已经第一次打开过了
                    doctorStrangeUpdater.afterFirstOpenInit();

                    showToast("初始化成功", DoctorStrangeUpdaterConstants.COLOR_SUCCESS);

            } catch (Exception e){
                    e.printStackTrace();
                    showToast("初始化失败"+e.getMessage(), DoctorStrangeUpdaterConstants.COLOR_ERROR);
                    Log.e(TAG, "initBeforeStartUp: " + e.getMessage());
                }
            }
        }
    }

    /**
     * show toast
     * @param msg
     * @param color
     */
    private void showToast(final String msg, final String color){

        UiThreadUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int colorInt = Color.parseColor(color);
                Log.e(TAG, "showToast: "+msg);
                Toast toast = new Toast(context);
                LayoutInflater inflater = LayoutInflater.from(context);
                View view = inflater.inflate(R.layout.toast, null);
                view.setBackgroundColor(colorInt);
                view.setMinimumWidth(widowWidth);
                TextView text = (TextView) view.findViewById(R.id.doctorToastText);
                text.setText(msg);
                toast.setView(view);
                toast.setDuration(Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP, 0, 0);
                toast.show();
            }
        });

    }



}
