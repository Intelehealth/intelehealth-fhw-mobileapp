package app.intelehealth.client.syncModule;

import android.os.Handler;
import android.os.Looper;

import androidx.work.WorkManager;

import app.intelehealth.client.app.AppConstants;
import app.intelehealth.client.app.IntelehealthApplication;
import app.intelehealth.client.database.dao.ImagesPushDAO;
import app.intelehealth.client.database.dao.SyncDAO;
import app.intelehealth.client.utilities.Logger;
import app.intelehealth.client.utilities.NotificationUtils;

public class SyncUtils {


    private static final String TAG = SyncUtils.class.getSimpleName();

    public void syncBackground() {
        SyncDAO syncDAO = new SyncDAO();
        ImagesPushDAO imagesPushDAO = new ImagesPushDAO();

        syncDAO.pushDataApi();

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                Logger.logD(TAG, "Pull data Started");
                syncDAO.pullData_Background(IntelehealthApplication.getAppContext()); //only this new function duplicate
                Logger.logD(TAG, "Pull data ended");
            }
        }, 3000);


        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                Logger.logD(TAG, "Patient Profile Image Push Started");
                imagesPushDAO.patientProfileImagesPush();
                Logger.logD(TAG, "Patient Profile  Image push ended");
            }
        }, 6000);


        /*
         * Looper.getMainLooper is used in background sync since the sync_background()
         * is called from the syncWorkManager.java class which executes the sync on the
         * worker thread (non-ui thread) and the image push is executing on the
         * ui thread.
         */

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                Logger.logD(TAG, "Background Image Push Started");
                imagesPushDAO.obsImagesPush();
                Logger.logD(TAG, "Background Image push ended");
            }
        }, 9000);

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                Logger.logD(TAG, "Background Image delete Started");
                imagesPushDAO.deleteObsImage();
                Logger.logD(TAG, "Background Image delete ended");
            }
        }, 12000);



        NotificationUtils notificationUtils = new NotificationUtils();
        notificationUtils.clearAllNotifications(IntelehealthApplication.getAppContext());

        //Background Sync Fixes : Chaining of request in place of running background service
        WorkManager.getInstance()
                .beginWith(AppConstants.VISIT_SUMMARY_WORK_REQUEST)
                .then(AppConstants.LAST_SYNC_WORK_REQUEST)
                .enqueue();

    }

    /**
     * This method will be responsible for initial sync/setup
     * @param fromActivity
     */
    public void initialSync(String fromActivity) {

        SyncDAO syncDAO = new SyncDAO();
        Logger.logD(TAG, "Pull Started");
        syncDAO.pullData(IntelehealthApplication.getAppContext(), fromActivity);
        Logger.logD(TAG, "Pull ended");

    }
    public boolean syncForeground(String fromActivity) {
        boolean isSynced = false;
        SyncDAO syncDAO = new SyncDAO();
        ImagesPushDAO imagesPushDAO = new ImagesPushDAO();
        Logger.logD(TAG, "Push Started");
        isSynced = syncDAO.pushDataApi();
        Logger.logD(TAG, "Push ended " + isSynced);


//        need to add delay for pulling the obs correctly
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Logger.logD(TAG, "Pull Started");
                syncDAO.pullData(IntelehealthApplication.getAppContext(), fromActivity);
                Logger.logD(TAG, "Pull ended");
            }
        }, 3000);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                imagesPushDAO.patientProfileImagesPush();
            }
        }, 6000);


//        imagesPushDAO.obsImagesPush();

        /*
         * Handler is added for pushing image in sync foreground
         * to fix the issue of Phy exam and additional images not showing up sometimes
         * on the webapp (doctor portal).
         * */
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Logger.logD(TAG, "Image Push Started");
                imagesPushDAO.obsImagesPush();
                Logger.logD(TAG, "Image Pull ended");
            }
        }, 9000);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                imagesPushDAO.deleteObsImage();
            }
        }, 12000);


        WorkManager.getInstance()
                .beginWith(AppConstants.VISIT_SUMMARY_WORK_REQUEST)
                .then(AppConstants.LAST_SYNC_WORK_REQUEST)
                .enqueue();

        /*Intent intent = new Intent(IntelehealthApplication.getAppContext(), UpdateDownloadPrescriptionService.class);
        IntelehealthApplication.getAppContext().startService(intent);*/

        return isSynced;
    }
}
