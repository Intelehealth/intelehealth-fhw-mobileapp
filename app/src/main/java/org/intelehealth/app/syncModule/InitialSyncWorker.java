package org.intelehealth.app.syncModule;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.database.dao.SyncDAO;
import org.intelehealth.app.models.dto.ResponseDTO;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.exception.DAOException;

public class InitialSyncWorker extends Worker {

    static ResponseDTO responseDTO;

    public InitialSyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    /**
     * Large amount of data passing not possible with intent
     * we passing data through static function
     *
     * @param dto
     */
    public static void setData(ResponseDTO dto) {
        responseDTO = dto;
    }

    /**
     * The reason of the worker is to push initial data to local db
     * to prevent ANR crash
     *
     */
    @NonNull
    @Override
    public Result doWork() {
        SessionManager sessionManager = SessionManager.getInstance(IntelehealthApplication.getAppContext());
        boolean sync = false;
        SyncDAO syncDAO = new SyncDAO();
        String fromActivity = getInputData().getString("from");
        try {
            sync = syncDAO.SyncData(responseDTO);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
        if (sync) {
            int nextPageNo = responseDTO.getData().getPageNo();
            int totalCount = responseDTO.getData().getTotalCount();
            int percentage = 0; // this should be only in initialSync....

            if (nextPageNo != -1) {
                percentage = (int) Math.round(nextPageNo * AppConstants.PAGE_LIMIT * 100.0 / totalCount);
                Logger.logD(SyncDAO.PULL_ISSUE, "percentage: " + percentage);
                SyncDAO.setProgress(percentage);
                syncDAO.pullDataBackgroundService(IntelehealthApplication.getAppContext(), fromActivity, nextPageNo);
            } else {
                percentage = 100;
                Logger.logD(SyncDAO.PULL_ISSUE, "percentage page -1: " + percentage);


                SyncDAO.setProgress(percentage);
                Intent broadcast = new Intent();
                broadcast.putExtra(AppConstants.SYNC_INTENT_DATA_KEY, AppConstants.SYNC_PULL_DATA_DONE);
                broadcast.setAction(AppConstants.SYNC_NOTIFY_INTENT_ACTION);

                sessionManager.setPullExcutedTime(sessionManager.isPulled());
                sessionManager.setLastSyncDateTime(AppConstants.dateAndTimeUtils.getcurrentDateTime(sessionManager.getAppLanguage()));

                getApplicationContext().sendBroadcast(broadcast);
                if (fromActivity.equalsIgnoreCase("home")) {
                    //Toast.makeText(context, context.getResources().getString(R.string.successfully_synced), Toast.LENGTH_LONG).show();
                } else if (fromActivity.equalsIgnoreCase("visitSummary")) {
                    //Toast.makeText(context, context.getResources().getString(R.string.visit_uploaded_successfully), Toast.LENGTH_LONG).show();
                } else if (fromActivity.equalsIgnoreCase("downloadPrescription")) {
//                            AppConstants.notificationUtils.DownloadDone(context.getString(R.string.download_from_doctor), context.getString(R.string.prescription_downloaded), 3, context);
//                            Toast.makeText(context, context.getString(R.string.prescription_downloaded), Toast.LENGTH_LONG).show();
                }
//                        else {
//                            Toast.makeText(context, context.getString(R.string.successfully_synced), Toast.LENGTH_LONG).show();
//                        }
            }

            return Result.success();
        } else {
//                        AppConstants.notificationUtils.DownloadDone(context.getString(R.string.sync), context.getString(R.string.failed_synced), 1, IntelehealthApplication.getAppContext());
            if (fromActivity.equalsIgnoreCase("home")) {
                // Toast.makeText(context, context.getString(R.string.failed_synced), Toast.LENGTH_LONG).show();
            } else if (fromActivity.equalsIgnoreCase("visitSummary")) {
                //Toast.makeText(context, context.getString(R.string.visit_not_uploaded), Toast.LENGTH_LONG).show();
            } else if (fromActivity.equalsIgnoreCase("downloadPrescription")) {
                // Toast.makeText(context, context.getString(R.string.prescription_not_downloaded_check_internet), Toast.LENGTH_LONG).show();
            }
//                        else {
//                            Toast.makeText(context, context.getString(R.string.failed_synced), Toast.LENGTH_LONG).show();
//                        }
            if (!sessionManager.isLogout()) {
                IntelehealthApplication.getAppContext().sendBroadcast(new Intent(AppConstants.SYNC_INTENT_ACTION)
                        .putExtra(AppConstants.SYNC_INTENT_DATA_KEY, AppConstants.SYNC_FAILED));
            }

            return Result.failure();
        }
    }
}
