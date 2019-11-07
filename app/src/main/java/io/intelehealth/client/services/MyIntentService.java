package io.intelehealth.client.services;

import android.app.IntentService;
import android.content.Intent;

import com.crashlytics.android.Crashlytics;

import io.intelehealth.client.utilities.Logger;

public class MyIntentService extends IntentService {

    public MyIntentService() {
        super("MyIntentService");
    }

    @Override
    protected void onHandleIntent(Intent arg0) {
        try {
            Thread.sleep(3000);
        } catch (Exception e) {
            Crashlytics.getInstance().core.logException(e);
            Logger.logE(MyIntentService.class.getSimpleName(), "Exception in onHandleIntent method", e);
        }

        Intent in = new Intent();
        in.setAction("OpenmrsID");
        sendBroadcast(in);


    }

}