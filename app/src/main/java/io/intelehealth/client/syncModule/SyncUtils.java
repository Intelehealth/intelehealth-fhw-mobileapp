package io.intelehealth.client.syncModule;

import io.intelehealth.client.app.IntelehealthApplication;
import io.intelehealth.client.database.dao.ImagesPushDAO;
import io.intelehealth.client.database.dao.PullDataDAO;
import io.intelehealth.client.utilities.NotificationUtils;

public class SyncUtils {


    public void Sync() {
        PullDataDAO pullDataDAO = new PullDataDAO();
        ImagesPushDAO imagesPushDAO = new ImagesPushDAO();

        pullDataDAO.pushDataApi();
        pullDataDAO.pullData(IntelehealthApplication.getAppContext());

        imagesPushDAO.patientProfileImagesPush();
        imagesPushDAO.obsImagesPush();

        NotificationUtils notificationUtils = new NotificationUtils();
        notificationUtils.clearAllNotifications(IntelehealthApplication.getAppContext());


    }
}
