package org.intelehealth.ekalarogya.syncModule;

import android.content.Context;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import androidx.annotation.NonNull;

import androidx.work.Worker;
import androidx.work.WorkerParameters;


import org.intelehealth.ekalarogya.database.dao.SyncDAO;
import org.intelehealth.ekalarogya.utilities.Logger;
import org.intelehealth.ekalarogya.utilities.SessionManager;

public class SyncWorkManager extends Worker {

    private SessionManager sessionManager = null;
    private String TAG = SyncWorkManager.class.getSimpleName();
    private Context context;

    public SyncWorkManager(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        sessionManager = new SessionManager(context);
        this.context=context;
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

        SyncUtils syncUtils = new SyncUtils();
        syncUtils.syncBackground();

        //---------Health Worker Status-----------
        //SyncDAO syncDAO = new SyncDAO();
        //syncDAO.syncUserStatus(context);

        return Result.success();
    }
}

