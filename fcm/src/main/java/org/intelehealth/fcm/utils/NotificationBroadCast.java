package org.intelehealth.fcm.utils;

import android.content.Context;
import android.content.Intent;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class NotificationBroadCast {
    public static final String CUSTOM_ACTION = "org.intelehealth.MODULE_FCM";
    public static final String FCM_MODULE = "fcm_module";

    public static void initialize(Context context) {
        // Perform initialization tasks for Module A
        Intent intent = new Intent(CUSTOM_ACTION);
        intent.putExtra(FCM_MODULE, "fcm");
        intent.setPackage("org.intelehealth.app");
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}