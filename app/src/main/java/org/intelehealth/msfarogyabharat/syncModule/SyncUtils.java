package org.intelehealth.msfarogyabharat.syncModule;

import android.os.Handler;
import android.os.Looper;

import androidx.work.WorkManager;

import org.intelehealth.msfarogyabharat.app.AppConstants;
import org.intelehealth.msfarogyabharat.app.IntelehealthApplication;
import org.intelehealth.msfarogyabharat.database.dao.ImagesPushDAO;
import org.intelehealth.msfarogyabharat.database.dao.SyncDAO;
import org.intelehealth.msfarogyabharat.utilities.Logger;
import org.intelehealth.msfarogyabharat.utilities.NotificationUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SyncUtils {


    private static final String TAG = SyncUtils.class.getSimpleName();

    public void initialSync(String fromActivity) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        executorService.execute(() -> {
            SyncDAO syncDAO = new SyncDAO();
            Logger.logD(TAG, "Pull Started");
            syncDAO.pullData(IntelehealthApplication.getAppContext(), fromActivity, 0); // pageNo = 0 since its the first call.
            Logger.logD(TAG, "Pull ended");
        });
    }

    public void syncBackground() {
        SyncDAO syncDAO = new SyncDAO();
        ImagesPushDAO imagesPushDAO = new ImagesPushDAO();

        syncDAO.pushDataApi();
        syncDAO.pullData_Background(IntelehealthApplication.getAppContext(), 0); //only this new function duplicate // pageNo = 0 since first call to function.

        imagesPushDAO.patientProfileImagesPush();
//        imagesPushDAO.obsImagesPush();

        /*
         * Looper.getMainLooper is used in background sync since the sync_background()
         * is called from the syncWorkManager.java class which executes the sync on the
         * worker thread (non-ui thread) and the image push is executing on the
         * ui thread.
         */
        final Handler handler_background = new Handler(Looper.getMainLooper());
        handler_background.postDelayed(new Runnable() {
            @Override
            public void run() {
                Logger.logD(TAG, "Background Image Push Started");
                imagesPushDAO.obsImagesPush();
                Logger.logD(TAG, "Background Image Pull ended");
            }
        }, 3000);

        imagesPushDAO.deleteObsImage();

        NotificationUtils notificationUtils = new NotificationUtils();
        notificationUtils.clearAllNotifications(IntelehealthApplication.getAppContext());

        //Background Sync Fixes : Chaining of request in place of running background service
        WorkManager.getInstance()
                .beginWith(AppConstants.VISIT_SUMMARY_WORK_REQUEST)
                .then(AppConstants.LAST_SYNC_WORK_REQUEST)
                .enqueue();

    }

    public boolean syncForeground(String fromActivity) {
        ExecutorService pullDataExecutor = Executors.newSingleThreadExecutor();
        ExecutorService imagesPushExecutor = Executors.newSingleThreadExecutor();

        boolean isSynced = false;
        SyncDAO syncDAO = new SyncDAO();
        ImagesPushDAO imagesPushDAO = new ImagesPushDAO();
        Logger.logD(TAG, "Push Started");
        isSynced = syncDAO.pushDataApi();
        Logger.logD(TAG, "Push ended");


//        need to add delay for pulling the obs correctly
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> {
            Logger.logD(TAG, "Pull Started");
            pullDataExecutor.execute(() -> syncDAO.pullData(IntelehealthApplication.getAppContext(), fromActivity, 0)); // pageNo = 0 since its first call.
            Logger.logD(TAG, "Pull ended");
        }, 3000);

        imagesPushDAO.patientProfileImagesPush();

//        imagesPushDAO.obsImagesPush();

        /*
         * Handler is added for pushing image in sync foreground
         * to fix the issue of Phy exam and additional images not showing up sometimes
         * on the webapp (doctor portal).
         * */
        final Handler handler_foreground = new Handler(Looper.getMainLooper());
        handler_foreground.postDelayed(() -> {
            Logger.logD(TAG, "Image Push Started");
            imagesPushExecutor.execute(imagesPushDAO::obsImagesPush);
            Logger.logD(TAG, "Image Pull ended");
        }, 3000);

        imagesPushDAO.deleteObsImage();


        WorkManager.getInstance()
                .beginWith(AppConstants.VISIT_SUMMARY_WORK_REQUEST)
                .then(AppConstants.LAST_SYNC_WORK_REQUEST)
                .enqueue();

        /*Intent intent = new Intent(IntelehealthApplication.getAppContext(), UpdateDownloadPrescriptionService.class);
        IntelehealthApplication.getAppContext().startService(intent);*/

        return isSynced;
    }
}
