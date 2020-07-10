package app.intelehealth.client.services;

import android.app.IntentService;
import android.content.Intent;


import com.google.firebase.crashlytics.FirebaseCrashlytics;

import app.intelehealth.client.utilities.Logger;

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
        sendBroadcast(in);
    }
}
