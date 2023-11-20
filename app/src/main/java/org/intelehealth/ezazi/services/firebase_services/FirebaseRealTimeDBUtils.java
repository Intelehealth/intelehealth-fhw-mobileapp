package org.intelehealth.ezazi.services.firebase_services;



import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.intelehealth.ezazi.BuildConfig;
import org.intelehealth.ezazi.app.AppConstants;
import org.intelehealth.ezazi.app.IntelehealthApplication;
import org.intelehealth.ezazi.utilities.SessionManager;

import java.util.Map;

public class FirebaseRealTimeDBUtils {
    public static final String FIREBASE_REAL_TIME_DB_BASE_URL = BuildConfig.REAL_TIME_FB_URL;
    public static final String FIREBASE_REAL_TIME_DB_BASE_LOG_REF = "log/";


    public static String getFirebaseRTDBUrl() {
        return FIREBASE_REAL_TIME_DB_BASE_URL;

    }

    public static String getFirebaseRTDBRootRef() {
        return new SessionManager(IntelehealthApplication.getAppContext()).getServerUrl().replaceAll("\\.","_")+"/"+FIREBASE_REAL_TIME_DB_BASE_LOG_REF;

    }
    public static void logData(Map<String, String> log) {
        FirebaseDatabase database = FirebaseDatabase.getInstance(getFirebaseRTDBUrl());
        DatabaseReference myRef = database.getReference(getFirebaseRTDBRootRef() + "/" +new SessionManager(IntelehealthApplication.getAppContext()).getProviderID()+ "/" + System.currentTimeMillis());
        myRef.setValue(log);
    }


}
