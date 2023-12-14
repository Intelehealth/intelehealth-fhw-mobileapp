package org.intelehealth.kf.services.firebase_services;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.intelehealth.kf.app.AppConstants;
import org.intelehealth.kf.app.IntelehealthApplication;
import org.intelehealth.kf.utilities.SessionManager;


public class DeviceInfoUtils {
    public static void saveDeviceInfo(Context context) {
        FirebaseDatabase database = FirebaseDatabase.getInstance(AppConstants.getFirebaseRTDBUrl());
        DatabaseReference myRef = database.getReference(AppConstants.getFirebaseRTDBRootRefForDeviceInfo() + getDeviceId());
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setOsVersion(getDeviceVersion());
        deviceInfo.setDeviceMake(getDeviceMake());
        deviceInfo.setDeviceModel(getDeviceModel());
        deviceInfo.setUserUUID(new SessionManager(context).getProviderID());
        deviceInfo.setUserName(new SessionManager(context).getChwname());
        myRef.setValue(deviceInfo);


    }

    public static String getDeviceMake() {
        return Build.BRAND;

    }

    public static String getDeviceModel() {
        return Build.MODEL;

    }

    public static String getDeviceVersion() {
        return Build.VERSION.RELEASE;

    }

    public static String getDeviceId() {
        return Settings.Secure.getString(IntelehealthApplication.getAppContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);

    }

}
