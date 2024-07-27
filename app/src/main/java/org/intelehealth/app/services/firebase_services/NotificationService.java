package org.intelehealth.app.services.firebase_services;

import android.content.Context;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import org.intelehealth.app.utilities.CustomLog;

public class NotificationService extends NotificationListenerService {
    private String TAG = this.getClass().getSimpleName();
    Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        CustomLog.i(TAG, "********** onNotificationPosted");
        CustomLog.i(TAG, "ID :" + sbn.getId() + " \t " + sbn.getNotification().tickerText + " \t " + sbn.getPackageName());

    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        CustomLog.i(TAG, "********** onNotificationRemoved");
        CustomLog.i(TAG, "ID :" + sbn.getId() + " \t " + sbn.getNotification().tickerText + " \t " + sbn.getPackageName());

    }

}