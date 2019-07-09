package io.intelehealth.client.syncModule;

import android.content.Context;
import android.support.annotation.NonNull;

import androidx.work.Worker;
import androidx.work.WorkerParameters;

import io.intelehealth.client.utilities.Logger;
import io.intelehealth.client.utilities.SessionManager;

public class SyncWorkManager extends Worker {

    SessionManager sessionManager = null;
    String TAG = SyncWorkManager.class.getSimpleName();

    public SyncWorkManager(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        sessionManager = new SessionManager(context);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context applicationContext = getApplicationContext();
        Logger.logD(TAG, "result job");

//        PullDataDAO pullDataDAO = new PullDataDAO();
//        ImagesPushDAO imagesPushDAO = new ImagesPushDAO();
//
////        if (pull)
////            sendNotification("Sync", "Synced Data");
////        else
////            sendNotification("Sync", "failed to Sync");
//
//        pullDataDAO.pushDataApi();
//
////        if (push)
////            sendNotification("Sync", "Synced Data");
////        else
////            sendNotification("Sync", "failed to Sync");
////
//
//        imagesPushDAO.patientProfileImagesPush();
//        imagesPushDAO.obsImagesPush();
//
//        pullDataDAO.pullData(applicationContext);
        SyncUtils syncUtils = new SyncUtils();
        syncUtils.Sync();

        return Result.success();
    }
}

