package org.intelehealth.msfarogyabharat.syncModule;

import android.content.Context;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import androidx.annotation.NonNull;

import androidx.work.Worker;
import androidx.work.WorkerParameters;


import org.intelehealth.msfarogyabharat.utilities.Logger;
import org.intelehealth.msfarogyabharat.utilities.SessionManager;

import java.util.concurrent.Executors;

public class SyncWorkManager extends Worker {

    private SessionManager sessionManager = null;
    private String TAG = SyncWorkManager.class.getSimpleName();

    public SyncWorkManager(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        sessionManager = new SessionManager(context);
    }


    @NonNull
    @Override
    public Result doWork() {
        try {
            Thread.sleep(3000);
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            Logger.logE(TAG, "Exception in doWork method", e);
        }
        Logger.logD(TAG, "doWork");
        //Logger.logD(TAG, "result job");

        Executors.newSingleThreadExecutor().execute(() -> {
            SyncUtils syncUtils = new SyncUtils();
            syncUtils.syncBackground();
        });

        return Result.success();
    }
}

