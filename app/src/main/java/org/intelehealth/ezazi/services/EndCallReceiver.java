package org.intelehealth.ezazi.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;

import org.intelehealth.ezazi.services.firebase_services.CallListenerBackgroundService;

public class EndCallReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        Bundle bundle = intent.getExtras();
        if (bundle == null) {
            return;
        }

        String state = bundle.getString(TelephonyManager.EXTRA_STATE);

        if (state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_IDLE)) {
            Intent serviceIntent = new Intent(context, CallListenerBackgroundService.class);

            Log.v("EndCallReceiver", "Call ended");
            if (CallListenerBackgroundService.isInstanceCreated()) {
                context.stopService(serviceIntent);
            }
            context.startService(serviceIntent);
        }

    }
}
