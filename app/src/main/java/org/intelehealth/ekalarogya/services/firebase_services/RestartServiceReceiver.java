package org.intelehealth.ekalarogya.services.firebase_services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.content.ContextCompat;

public class RestartServiceReceiver extends BroadcastReceiver {

    private static final String TAG = RestartServiceReceiver.class.getName();

    @Override
    public void onReceive(Context context, Intent intent) {
         /* below change is done as a solution to the crash reported in firebase,
        To resolve it we have the SDK version and then can start it either as a foreground service
         or just with startService. */

        if (!CallListenerBackgroundService.isInstanceCreated()) {
            Intent serviceIntent = new Intent(context, CallListenerBackgroundService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ContextCompat.startForegroundService(context, serviceIntent);
            }
            else
                context.startService(serviceIntent);
        }
    }
}