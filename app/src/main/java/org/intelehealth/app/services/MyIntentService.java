package org.intelehealth.app.services;

import android.app.IntentService;
import android.content.Intent;


import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.utilities.Logger;

public class MyIntentService extends IntentService {

    public MyIntentService() {
        super("MyIntentService");
    }

    @Override
    protected void onHandleIntent(Intent arg0) {
        try {
            Thread.sleep(3000);
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            Logger.logE(MyIntentService.class.getSimpleName(), "Exception in onHandleIntent method", e);
        }

        Intent in = new Intent();
        in.setPackage(IntelehealthApplication.getInstance().getPackageName());
        in.setAction("OpenmrsID");
        sendBroadcast(in);


    }

}