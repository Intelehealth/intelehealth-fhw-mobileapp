package org.intelehealth.ezazi.services.firebase_services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.intelehealth.ezazi.activities.homeActivity.HomeActivity;
import org.intelehealth.klivekit.utils.RtcUtilsKt;


public class CallRTCNotifyReceiver extends BroadcastReceiver {

    private static final String TAG = CallRTCNotifyReceiver.class.getName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG, "onReceive");
        Intent in = new Intent(context, HomeActivity.class);
        in.putExtras(intent.getExtras());
        in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(in);
    }
}