package io.intelehealth.client.syncModule;

import android.content.Intent;

import io.intelehealth.client.app.IntelehealthApplication;
import io.intelehealth.client.database.dao.ImagesPushDAO;
import io.intelehealth.client.database.dao.PullDataDAO;
import io.intelehealth.client.services.UpdateDownloadPrescriptionService;
import io.intelehealth.client.utilities.NotificationUtils;

public class SyncUtils {


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

        isSynced = pullDataDAO.pushDataApi();
        isSynced = pullDataDAO.pullData(IntelehealthApplication.getAppContext());

        imagesPushDAO.patientProfileImagesPush();
        imagesPushDAO.obsImagesPush();
        imagesPushDAO.deleteObsImage();

        Intent intent = new Intent(IntelehealthApplication.getAppContext(), UpdateDownloadPrescriptionService.class);
        IntelehealthApplication.getAppContext().startService(intent);

        return isSynced;
    }
}
