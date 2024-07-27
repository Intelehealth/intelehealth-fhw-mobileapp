package org.intelehealth.app.syncModule;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import org.intelehealth.app.utilities.CustomLog;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.WorkManager;

import com.google.gson.Gson;

import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.database.dao.followup_notification.FollowUpNotificationDAO;
import org.intelehealth.app.models.FollowUpNotificationData;
import org.intelehealth.app.models.FollowUpNotificationShData;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.NotificationSchedulerUtils;
import org.intelehealth.app.utilities.SessionManager;

import java.util.List;

public class BootCompleteReceiver extends BroadcastReceiver {
    private static final String TAG = BootCompleteReceiver.class.getSimpleName();

    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() == null || !intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Logger.logD(TAG, "onrecieve");
            return;
        }
        WorkManager.getInstance(IntelehealthApplication.getAppContext()).enqueueUniquePeriodicWork(AppConstants.UNIQUE_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, AppConstants.PERIODIC_WORK_REQUEST);

        //normally alarm manager are not works after reboot
        //handling scheduled alarm after reboot here
        List<FollowUpNotificationShData> notificationList = new FollowUpNotificationDAO().getFollowupNotification();
        for(FollowUpNotificationShData data : notificationList){
            NotificationSchedulerUtils.scheduleNotificationAlarmManager(
                    Long.parseLong(data.getDateTime()),
                    Long.parseLong(data.getDuration()),
                    true,
                    new FollowUpNotificationData(
                          data.getValue(),
                          data.getName(),
                          data.getOpenMrsId(),
                          data.getPatientUid(),
                          data.getVisitUuid()
                    )
            );
        }
    }
}