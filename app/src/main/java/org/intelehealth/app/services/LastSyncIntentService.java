package org.intelehealth.app.services;

import android.app.IntentService;
import android.content.Intent;


import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.utilities.Logger;

public class LastSyncIntentService extends IntentService {
    public LastSyncIntentService() {
        super("LastSyncIntentService");
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            Thread.sleep(3000);
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            Logger.logE(LastSyncIntentService.class.getSimpleName(), "Exception in onHandleIntent method", e);
        }

        Intent in = new Intent();
        in.setAction("lasysync");
        in.setPackage(IntelehealthApplication.getAppContext().getPackageName());
        sendBroadcast(in);
    }
}
