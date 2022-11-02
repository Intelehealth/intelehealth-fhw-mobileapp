package org.intelehealth.ekalarogya.services.firebase_services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.intelehealth.apprtc.CompleteActivity;
import org.intelehealth.ekalarogya.activities.homeActivity.HomeActivity;

public class CallRTCNotifyReceiver extends BroadcastReceiver {

    private static final String TAG = CallRTCNotifyReceiver.class.getName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG, "onReceive");
        Bundle bundle = intent.getExtras();
        boolean isCallEnded = bundle.getBoolean("callEnded");

        Intent in;
        if (isCallEnded) {
            in = new Intent(context, CompleteActivity.class);
            in.putExtras(intent.getExtras());
        } else {
            in = new Intent(context, HomeActivity.class);
            in.putExtras(intent.getExtras());
            in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
        context.startActivity(in);
    }
}
