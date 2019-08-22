package io.intelehealth.client.services;

import android.app.IntentService;
import android.content.Intent;

import com.crashlytics.android.Crashlytics;

import io.intelehealth.client.utilities.Logger;

public class LastSyncIntentService extends IntentService {
    public LastSyncIntentService() {
        super("LastSyncIntentService");
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
//    public LastSyncIntentService(String name) {
//        super(name);
//    }
    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            Thread.sleep(3000);
        } catch (Exception e) {
            Crashlytics.getInstance().core.logException(e);
            Logger.logE(LastSyncIntentService.class.getSimpleName(), "Exception in onHandleIntent method", e);
        }

        Intent in = new Intent();
        in.setAction("lasysync");
        sendBroadcast(in);
    }
}
