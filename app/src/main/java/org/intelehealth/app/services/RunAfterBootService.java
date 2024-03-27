package org.intelehealth.app.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.WorkManager;

import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.app.AppConstants;

public class RunAfterBootService extends Service {
    String TAG = RunAfterBootService.class.getSimpleName();

    public RunAfterBootService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.logD(TAG, "RunAfterBootService onCreate() method.");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        WorkManager.getInstance(IntelehealthApplication.getAppContext()).enqueueUniquePeriodicWork(AppConstants.UNIQUE_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, AppConstants.PERIODIC_WORK_REQUEST);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
