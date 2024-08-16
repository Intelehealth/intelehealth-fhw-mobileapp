package org.intelehealth.nak.syncModule;

import android.content.Context;
import android.content.Intent;


import com.google.firebase.crashlytics.FirebaseCrashlytics;

import androidx.annotation.NonNull;
import androidx.work.ListenableWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;


import org.intelehealth.nak.app.AppConstants;
import org.intelehealth.nak.app.IntelehealthApplication;
import org.intelehealth.nak.utilities.Logger;
import org.intelehealth.nak.utilities.SessionManager;


public class LastSyncWork extends Worker {

    private SessionManager sessionManager = null;
    private String TAG = VisitSummaryWork.class.getSimpleName();

    public LastSyncWork(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        sessionManager = new SessionManager(context);
    }


    @NonNull
    @Override
    public ListenableWorker.Result doWork() {

        try {
            Thread.sleep(3000);
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            Logger.logE(TAG, "Exception in doWork method", e);
        }
        Logger.logD(TAG, "doWork");

        Intent in = new Intent();
        in.setAction(AppConstants.SYNC_INTENT_ACTION);
        in.setPackage(IntelehealthApplication.getInstance().getPackageName());
        IntelehealthApplication.getAppContext().sendBroadcast(in);

        return Result.success();
    }
}