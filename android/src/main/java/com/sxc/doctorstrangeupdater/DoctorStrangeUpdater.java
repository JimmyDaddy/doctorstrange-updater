package com.sxc.doctorstrangeupdater;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.AsyncTask;
import android.os.Looper;
import android.os.PowerManager;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;


public class DoctorStrangeUpdater {

    public static final String DOCTOR_SHARED_PREFERENCES = "Doctorstrange_Updater_Shared_Preferences";
    public static final String JS_VERSION = "Doctorstrange_Updater_Stored_Version";
    private final String LAST_UPDATE_TIMESTAMP = "Doctorstrange_Updater_Last_Update_Timestamp";
    private final String JS_BUNDLENAME = "doctor.jsbundle";
    private final String JS_FOLDER = "JSCode";


    private static DoctorStrangeUpdater ourInstance = new DoctorStrangeUpdater();
    private String updateMetadataUrl;
    private String metadataAssetName;
    private Context context;
    private boolean showInfo = true;//是否显示Toast

    public static DoctorStrangeUpdater getInstance(Context context) {
        ourInstance.context = context;
        return ourInstance;
    }

    private DoctorStrangeUpdater() {
    }

    public DoctorStrangeUpdater setUpdateMetadataUrl(String url) {
        this.updateMetadataUrl = url;
        return this;
    }

    public DoctorStrangeUpdater setMetadataAssetName(String metadataAssetName) {
        this.metadataAssetName = metadataAssetName;
        return this;
    }


    public DoctorStrangeUpdater showProgress(boolean progress) {
        this.showInfo = progress;
        return this;
    }


    public String getLatestJSCodeLocation() {
        SharedPreferences prefs = context.getSharedPreferences(DOCTOR_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        String currentVersionStr = prefs.getString(JS_VERSION, null);

        Version currentVersion;
        try {
            currentVersion = new Version(currentVersionStr);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        String jsonString = this.getStringFromAsset(this.metadataAssetName);
        if (jsonString == null) {
            return null;
        } else {
            String jsCodePath = null;
            try {
                JSONObject assetMetadata = new JSONObject(jsonString);
                String assetVersionStr = assetMetadata.getString("version");
                Version assetVersion = new Version(assetVersionStr);

                if (currentVersion.compareTo(assetVersion) > 0) {
                    File jsCodeDir = context.getDir(JS_FOLDER, Context.MODE_PRIVATE);
                    File jsCodeFile = new File(jsCodeDir, JS_BUNDLENAME);
                    jsCodePath = jsCodeFile.getAbsolutePath();
                } else {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(JS_VERSION, currentVersionStr);
                    editor.apply();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return jsCodePath;
        }
    }

    private String getStringFromAsset(String assetName) {
        String jsonString = null;
        try {
            InputStream inputStream = this.context.getAssets().open(assetName);
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            jsonString = new String(buffer, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonString;
    }



    private String getContainerVersion() {
        String version = null;
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            version = pInfo.versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return version;
    }


    private void showToast(int message) {
        if (this.showInfo) {
            if (Looper.myLooper() == null)
                Looper.prepare();
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, message, duration);
            toast.show();
//            Looper.loop();
        }
    }

}
