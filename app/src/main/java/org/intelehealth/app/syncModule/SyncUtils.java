package org.intelehealth.app.syncModule;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import androidx.work.WorkManager;

import org.intelehealth.app.R;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.appointment.sync.AppointmentSync;
import org.intelehealth.app.database.dao.ImagesPushDAO;
import org.intelehealth.app.database.dao.SyncDAO;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.app.utilities.NotificationUtils;
import org.intelehealth.app.utilities.SessionManager;

public class SyncUtils {


    private static final String TAG = SyncUtils.class.getSimpleName();

    /**
     * This method will be responsible for initial sync/setup
     *
     * @param fromActivity
     */
    public void initialSync(String fromActivity,Context context) {

        SyncDAO syncDAO = new SyncDAO();
        Logger.logD(TAG, "Pull Started");
        syncDAO.pullDataBackgroundService(IntelehealthApplication.getAppContext(), fromActivity,0);
        Logger.logD(TAG, "Pull ended");
        // sync data
        AppointmentSync.getAppointments(context);
    }

    public void syncInBackground() {
        SyncDAO syncDAO = new SyncDAO();
        ImagesPushDAO imagesPushDAO = new ImagesPushDAO();
        SessionManager sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        syncDAO.pushDataApi();
        syncDAO.pullData_Background(IntelehealthApplication.getAppContext(), 0);
        imagesPushDAO.loggedInUserProfileImagesPush();
        if (!sessionManager.isLogout()) {
            AppointmentSync.getAppointments(IntelehealthApplication.getAppContext());
        }

    }

    public void syncBackground() {
        SyncDAO syncDAO = new SyncDAO();
        ImagesPushDAO imagesPushDAO = new ImagesPushDAO();
        SessionManager sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        /*
         * Looper.getMainLooper is used in background sync since the sync_background()
         * is called from the syncWorkManager.java class which executes the sync on the
         * worker thread (non-ui thread) and the image push is executing on the
         * ui thread.
         *
         * The image push itself is now triggered from the metadata push's completion
         * callback (success or error) instead of a fixed delay, so it can no longer
         * fire before the parent encounter/obs exists on the server - which was
         * silently dropping images on slower syncs.
         */
        final Handler handler_background = new Handler(Looper.getMainLooper());
        syncDAO.pushDataApi(success -> handler_background.post(() -> {
            //sometimes syncing happening while logout
            //added the checking to prevent appointment api call
            if(!sessionManager.isLogout()){
                AppointmentSync.getAppointments(IntelehealthApplication.getAppContext());
            }
            Logger.logD(TAG, "Background Image Push Started, metadata push success=" + success);
            imagesPushDAO.obsImagesPush();
            Logger.logD(TAG, "Background Image Push triggered");
        }));
        syncDAO.pullData_Background(IntelehealthApplication.getAppContext(),0); //only this new function duplicate
        imagesPushDAO.loggedInUserProfileImagesPush();

        imagesPushDAO.deleteObsImage();

        IntelehealthApplication.getAppContext().sendBroadcast(new Intent(AppConstants.SYNC_INTENT_ACTION)
                .putExtra(AppConstants.SYNC_INTENT_DATA_KEY, AppConstants.ALL_SYNC_DONE)
                .setPackage(IntelehealthApplication.getAppContext().getPackageName()));

        NotificationUtils notificationUtils = new NotificationUtils();
        notificationUtils.clearAllNotifications(IntelehealthApplication.getAppContext());
        WorkManager.getInstance(IntelehealthApplication.getAppContext())
                .beginWith(AppConstants.VISIT_SUMMARY_WORK_REQUEST)
                .then(AppConstants.LAST_SYNC_WORK_REQUEST)
                .enqueue();

    }


    public boolean syncForeground(String fromActivity) {
        boolean isSynced = false;
        SyncDAO syncDAO = new SyncDAO();
        ImagesPushDAO imagesPushDAO = new ImagesPushDAO();
        Logger.logD(TAG, "Push Started");
        /*
         * Obs images (physical exam / additional docs) are pushed only once the
         * encounter/obs metadata push actually completes (success or error) rather
         * than after a fixed delay - a fixed delay can fire before the parent
         * encounter/obs exists on the server, which silently drops the image
         * upload and caused images to sometimes not show up on the webapp
         * (doctor portal).
         */
        final Handler mainHandler = new Handler(Looper.getMainLooper());
        isSynced = syncDAO.pushDataApi(success -> mainHandler.post(() -> {
            Logger.logD(TAG, "Image Push Started, metadata push success=" + success);
            imagesPushDAO.obsImagesPush();
            Logger.logD(TAG, "Image Push triggered");
        }));
        Logger.logD(TAG, "Push ended");
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Logger.logD(TAG, "Pull Started");
                syncDAO.pullData(IntelehealthApplication.getAppContext(), fromActivity,0);
                AppointmentSync.getAppointments(IntelehealthApplication.getAppContext());
                Logger.logD(TAG, "Pull ended");
            }
        }, 6000);

        imagesPushDAO.patientProfileImagesPush();
        //ui2.0
        imagesPushDAO.loggedInUserProfileImagesPush();

        imagesPushDAO.deleteObsImage();

        WorkManager.getInstance(IntelehealthApplication.getAppContext())
                .beginWith(AppConstants.VISIT_SUMMARY_WORK_REQUEST)
                .then(AppConstants.LAST_SYNC_WORK_REQUEST)
                .enqueue();

        /*Intent intent = new Intent(IntelehealthApplication.getAppContext(), UpdateDownloadPrescriptionService.class);
        IntelehealthApplication.getAppContext().startService(intent);*/

        return isSynced;
    }

    /**
     * Clicking on this btn will start Sync.
     *
     * @param view Refresh button view.
     */
    public static boolean syncNow(Context context, View view, ObjectAnimator syncAnimator) {
        boolean isSyncedValue = false;

        final ObjectAnimator animator = ObjectAnimator.ofFloat(view, View.ROTATION, 0f, 359f).setDuration(1200);
        animator.setInterpolator(new LinearInterpolator());

        if (NetworkConnection.isOnline(context)) {
            //Toast.makeText(context, context.getString(R.string.sync_strated), Toast.LENGTH_SHORT).show();
            view.clearAnimation();
            animator.start();
            
            new Thread(new Runnable() {
                @Override
                public void run() {
                    new SyncUtils().syncBackground();
                }
            }).start();

            isSyncedValue = true;
            new Handler(Looper.getMainLooper())
                    .postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //Toast.makeText(context, context.getString(R.string.successfully_synced), Toast.LENGTH_SHORT).show();
                        }
                    }, 1200);

        } else {
            isSyncedValue = false;
            Toast.makeText(context, context.getString(R.string.failed_synced), Toast.LENGTH_LONG).show();
        }

        return isSyncedValue;
    }

}
