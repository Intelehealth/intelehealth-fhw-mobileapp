package app.intelehealth.client.services;

import android.app.IntentService;
import android.content.Intent;

import com.crashlytics.android.Crashlytics;

import app.intelehealth.client.utilities.Logger;

public class UpdateDownloadPrescriptionService extends IntentService {

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public UpdateDownloadPrescriptionService() {
        super("UpdateDownloadPrescriptionService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            Intent in = new Intent();
            in.setAction("downloadprescription");
            sendBroadcast(in);
        } catch (Exception e) {
            Crashlytics.getInstance().core.logException(e);
            Logger.logE(UpdateDownloadPrescriptionService.class.getSimpleName(), "Exception in onHandleIntent method", e);
        }


    }
}
