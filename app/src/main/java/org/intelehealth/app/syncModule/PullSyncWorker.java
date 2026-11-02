package org.intelehealth.app.syncModule;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.database.dao.SyncDAO;
import org.intelehealth.app.utilities.Logger;

import java.util.concurrent.TimeUnit;

/**
 * Runs the paged background pull and stays alive until the last page is done.
 * <p>
 * The pull is a chain of async Retrofit calls. Started from a worker that returns right away,
 * nothing keeps the app running, so when the screen locks Android freezes the process and the
 * chain stalls mid-way. Keeping this job running until the chain ends prevents that.
 */
public class PullSyncWorker extends Worker {

    private static final String TAG = PullSyncWorker.class.getSimpleName();
    // Public so the home screen can observe it and keep the sync icon rotating while it runs.
    public static final String UNIQUE_WORK_NAME = "background_pull_sync";
    // WorkManager stops a job after 10 minutes; return before that and retry instead.
    private static final long MAX_WAIT_MINUTES = 9;

    public PullSyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    public static void enqueue(Context context) {
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(PullSyncWorker.class)
                .setConstraints(AppConstants.MY_CONSTRAINTS)
                .build();
        // KEEP: a pull that is already queued or running covers this request.
        WorkManager.getInstance(context)
                .enqueueUniqueWork(UNIQUE_WORK_NAME, ExistingWorkPolicy.KEEP, request);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Returns false when a chain is already running (e.g. this is a retry after a
        // timeout); waiting on it below still keeps the app alive until it finishes.
        new SyncDAO().pullData_Background(getApplicationContext(), 0);
        try {
            if (!SyncDAO.awaitBackgroundPull(MAX_WAIT_MINUTES, TimeUnit.MINUTES)) {
                Logger.logD(SyncDAO.PULL_ISSUE, TAG + ": pull still running, retrying to keep waiting");
                return Result.retry();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Result.retry();
        }
        return SyncDAO.isLastBackgroundPullSucceeded() ? Result.success() : Result.failure();
    }
}
