package io.intelehealth.client.syncModule;

import android.content.Intent;

import io.intelehealth.client.app.IntelehealthApplication;
import io.intelehealth.client.database.dao.ImagesPushDAO;
import io.intelehealth.client.database.dao.PullDataDAO;
import io.intelehealth.client.services.UpdateDownloadPrescriptionService;
import io.intelehealth.client.utilities.Logger;
import io.intelehealth.client.utilities.NotificationUtils;

public class SyncUtils {


    private static final String TAG = SyncUtils.class.getSimpleName();

    public void syncBackground() {
        PullDataDAO pullDataDAO = new PullDataDAO();
        ImagesPushDAO imagesPushDAO = new ImagesPushDAO();

        pullDataDAO.pushDataApi();
        pullDataDAO.pullData(IntelehealthApplication.getAppContext());

        imagesPushDAO.patientProfileImagesPush();
        imagesPushDAO.obsImagesPush();
        imagesPushDAO.deleteObsImage();

        NotificationUtils notificationUtils = new NotificationUtils();
        notificationUtils.clearAllNotifications(IntelehealthApplication.getAppContext());

        Intent intent = new Intent(IntelehealthApplication.getAppContext(), UpdateDownloadPrescriptionService.class);
        IntelehealthApplication.getAppContext().startService(intent);

    }

    public boolean syncForeground() {
        boolean isSynced = false;
        PullDataDAO pullDataDAO = new PullDataDAO();
        ImagesPushDAO imagesPushDAO = new ImagesPushDAO();
        Logger.logD(TAG, "Push Started");
        isSynced = pullDataDAO.pushDataApi();
        Logger.logD(TAG, "Push ended");

        Logger.logD(TAG, "Pull Started");
        pullDataDAO.pullData(IntelehealthApplication.getAppContext());
        Logger.logD(TAG, "Pull ended");

        Logger.logD(TAG, "patient profile image push Started");
        imagesPushDAO.patientProfileImagesPush();
        Logger.logD(TAG, "patient profile image push ended");

        Logger.logD(TAG, "obs image started");
        imagesPushDAO.obsImagesPush();
        Logger.logD(TAG, "obs image ended");

        Logger.logD(TAG, "obs delete image started");
        imagesPushDAO.deleteObsImage();
        Logger.logD(TAG, "obs delete image ended");
        //need to add delay for pulling the obs correctly
//        final Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//
//            }
//        }, 3000);


        Intent intent = new Intent(IntelehealthApplication.getAppContext(), UpdateDownloadPrescriptionService.class);
        IntelehealthApplication.getAppContext().startService(intent);

        return isSynced;
    }
}
