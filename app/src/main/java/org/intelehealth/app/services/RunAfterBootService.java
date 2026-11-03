package org.intelehealth.app.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.optimized_sync.OptimizedSyncWorker;
import org.intelehealth.app.utilities.Logger;

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
        OptimizedSyncWorker.enqueuePeriodicWork(IntelehealthApplication.getAppContext());

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
