package org.intelehealth.app.services;

import android.app.IntentService;
import android.content.Intent;

import androidx.annotation.Nullable;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.database.dao.SyncDAO;
import org.intelehealth.app.models.dto.ResponseDTO;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.exception.DAOException;

public class InitialSyncIntentService extends IntentService {

    public InitialSyncIntentService() {
        super("InitialSyncIntentService");
    }

    static ResponseDTO responseDTO;

    /**
     * Large amount of data passing not possible with intent
     * we passing data through static function
     * @param dto
     */
    public static void setData(ResponseDTO dto){
        responseDTO = dto;
    }

    /**
     * The reason of the service is to push initial data to local db
     * to prevent ANR crash
     * @param intent
     */
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        SessionManager sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        boolean sync = false;
        String fromActivity = intent.getStringExtra("from");
        try {
            sync = new SyncDAO().SyncData(responseDTO);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
        if (sync) {
            sessionManager.setLastSyncDateTime(AppConstants.dateAndTimeUtils.getcurrentDateTime(sessionManager.getAppLanguage()));
            Intent broadcast = new Intent();
            broadcast.putExtra(AppConstants.SYNC_INTENT_DATA_KEY, AppConstants.SYNC_PULL_DATA_DONE);
            broadcast.setAction(AppConstants.SYNC_NOTIFY_INTENT_ACTION);
            sendBroadcast(broadcast);
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
        else {
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
            IntelehealthApplication.getAppContext().sendBroadcast(new Intent(AppConstants.SYNC_INTENT_ACTION)
                    .putExtra(AppConstants.SYNC_INTENT_DATA_KEY, AppConstants.SYNC_FAILED));
        }
    }
}