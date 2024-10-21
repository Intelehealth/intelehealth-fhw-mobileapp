package org.intelehealth.app.services;

import android.app.IntentService;
import android.content.Intent;


import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.utilities.Logger;

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
            in.setPackage(IntelehealthApplication.getAppContext().getPackageName());
            sendBroadcast(in);
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            Logger.logE(UpdateDownloadPrescriptionService.class.getSimpleName(), "Exception in onHandleIntent method", e);
        }


    }
}
