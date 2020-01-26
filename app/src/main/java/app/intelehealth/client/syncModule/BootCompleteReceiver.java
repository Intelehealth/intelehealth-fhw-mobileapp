package app.intelehealth.client.syncModule;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.WorkManager;

import app.intelehealth.client.app.AppConstants;
import app.intelehealth.client.utilities.Logger;

public class BootCompleteReceiver extends BroadcastReceiver {
    private static final String TAG = BootCompleteReceiver.class.getSimpleName();

    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() == null || !intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Logger.logD(TAG, "onrecieve");
            return;
        }
        WorkManager.getInstance().enqueueUniquePeriodicWork(AppConstants.UNIQUE_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, AppConstants.PERIODIC_WORK_REQUEST);

    }
}