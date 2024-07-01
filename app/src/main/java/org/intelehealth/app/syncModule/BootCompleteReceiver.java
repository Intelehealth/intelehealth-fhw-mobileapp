package org.intelehealth.app.syncModule;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.WorkManager;

import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.database.dao.FollowUpNotificationScheduleDAO;
import org.intelehealth.app.models.FollowUpNotificationShData;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.NotificationSchedulerUtils;

import java.util.List;

public class BootCompleteReceiver extends BroadcastReceiver {
    private static final String TAG = BootCompleteReceiver.class.getSimpleName();

    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() == null || !intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Logger.logD(TAG, "onrecieve");
            return;
        }
        WorkManager.getInstance(IntelehealthApplication.getAppContext()).enqueueUniquePeriodicWork(AppConstants.UNIQUE_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, AppConstants.PERIODIC_WORK_REQUEST);

        List<FollowUpNotificationShData> notificationList = new FollowUpNotificationScheduleDAO().getFollowupNotification();
        for(FollowUpNotificationShData data : notificationList){
           /* NotificationSchedulerUtils.scheduleNotificationAlarmManager(

            );*/
        }
    }
}