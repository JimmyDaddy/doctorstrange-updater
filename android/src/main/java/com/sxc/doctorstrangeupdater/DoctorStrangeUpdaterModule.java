package com.sxc.doctorstrangeupdaterupdater;

import android.content.Context;
import android.content.SharedPreferences;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * @author rahul
 */
public class DoctorStrangeUpdaterModule extends ReactContextBaseJavaModule {

    private ReactApplicationContext context;

    public DoctorStrangeUpdaterModule(ReactApplicationContext context) {
        super(context);
        this.context = context;
    }

    @Override
    public String getName() {
        return "DoctorStrangeUpdater";
    }

    @Nullable
    @Override
    public Map<String, Object> getConstants() {
        Map<String, Object> constants = new HashMap<String, Object>();
        SharedPreferences prefs = this.context.getSharedPreferences(
                DoctorStrangeUpdater.RNAU_SHARED_PREFERENCES, Context.MODE_PRIVATE
        );
        String version =  prefs.getString(DoctorStrangeUpdater.RNAU_STORED_VERSION, null);
        constants.put("jsCodeVersion", version);
        return constants;
    }
}
