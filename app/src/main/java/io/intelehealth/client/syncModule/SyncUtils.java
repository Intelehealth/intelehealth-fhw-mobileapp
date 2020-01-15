package io.intelehealth.client.syncModule;

import android.content.Intent;
import android.os.Handler;

import io.intelehealth.client.app.IntelehealthApplication;
import io.intelehealth.client.database.dao.ImagesPushDAO;
import io.intelehealth.client.database.dao.SyncDAO;
import io.intelehealth.client.services.UpdateDownloadPrescriptionService;
import io.intelehealth.client.utilities.Logger;
import io.intelehealth.client.utilities.NotificationUtils;

public class SyncUtils {


    private static final String TAG = SyncUtils.class.getSimpleName();

    public void syncBackground() {
        SyncDAO syncDAO = new SyncDAO();
        ImagesPushDAO imagesPushDAO = new ImagesPushDAO();

        syncDAO.pushDataApi();
        syncDAO.pullData_Background(IntelehealthApplication.getAppContext()); //only this new function duplicate

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
        SyncDAO syncDAO = new SyncDAO();
        ImagesPushDAO imagesPushDAO = new ImagesPushDAO();
        Logger.logD(TAG, "Push Started");
        isSynced = syncDAO.pushDataApi();
        Logger.logD(TAG, "Push ended");


//        need to add delay for pulling the obs correctly
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Logger.logD(TAG, "Pull Started");
                syncDAO.pullData(IntelehealthApplication.getAppContext());
                Logger.logD(TAG, "Pull ended");
            }
        }, 3000);

        imagesPushDAO.patientProfileImagesPush();

        imagesPushDAO.obsImagesPush();

        imagesPushDAO.deleteObsImage();

        Intent intent = new Intent(IntelehealthApplication.getAppContext(), UpdateDownloadPrescriptionService.class);
        IntelehealthApplication.getAppContext().startService(intent);

        return isSynced;
    }
}
