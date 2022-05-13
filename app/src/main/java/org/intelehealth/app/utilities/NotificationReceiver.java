package org.intelehealth.app.utilities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;
import android.view.WindowManager;

/**
* Created by Prajwal Maruti Waingankar on 12-05-2022, 11:29
* Copyright (c) 2021 . All rights reserved.
* Email: prajwalwaingankar@gmail.com
* Github: prajwalmw
*/

public class NotificationReceiver extends BroadcastReceiver {
    PowerManager.WakeLock wl;

    @Override
    public void onReceive(Context context, Intent intent) {

        NotificationUtils notificationUtils = new NotificationUtils();
        notificationUtils.createTimelineNotification(context, intent);

        PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = pm.isScreenOn();

        if(isScreenOn==false)
        {
            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK |PowerManager.ACQUIRE_CAUSES_WAKEUP |
                    PowerManager.ON_AFTER_RELEASE,"myApp:notificationLock");
            wl.acquire(1000);

            PowerManager.WakeLock wl_cpu = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"myApp:mycpu_notificationLock");
            wl_cpu.acquire(1000);

            // Official Doc: acquire() -> Ensures that the device is on at the level requested when the wake lock was created.
            // The lock will be released after the given timeout expires.
        }

    }
}
