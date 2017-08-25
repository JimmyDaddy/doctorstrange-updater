package com.sxc.doctorstrangeupdater;

import android.content.Context;
import android.util.Log;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.JavaScriptModule;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.sxc.doctorstrangeupdater.DoctorStrangeUpdaterConstants.ASSETS_BUNDLE_PREFIX;

public class DoctorStrangeUpdaterPackage implements ReactPackage {

    private String defaultMetaDataName;

    private static Context mContext;

    public DoctorStrangeUpdaterPackage(Context context, String defaultMetaDataName){
        this.defaultMetaDataName = defaultMetaDataName;
        mContext = context;
    }

    @Override
    public List<NativeModule> createNativeModules(ReactApplicationContext reactContext) {
        List<NativeModule> list = new ArrayList<NativeModule>();
        list.add(DoctorStrangeUpdaterModule.getInstance(reactContext, this.defaultMetaDataName));
        list.add(new DoctorFSManager(reactContext));
        return list;
    }

    @Override
    public List<ViewManager> createViewManagers(ReactApplicationContext reactContext) {
        return Collections.emptyList();
    }

    /**
     *
     * @return
     */
    public static String getJSBundleFile(){
        String jsbundleFileStr = mContext.getDir("Library", Context.MODE_PRIVATE) + "/" + DoctorStrangeUpdaterConstants.SOURCE_ROOT + "/" + DoctorStrangeUpdaterConstants.BUNDLE_NAME;

        File jsbundle = new File(jsbundleFileStr);

        if(jsbundle.exists()){
            return jsbundleFileStr;
        } else {
            return ASSETS_BUNDLE_PREFIX + DoctorStrangeUpdaterConstants.DEFAULT_JSBUNDLE_ASSET_NAME;
        }
    }
}
