package org.intelehealth.ezazi.services.firebase_services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.intelehealth.ezazi.activities.homeActivity.HomeActivity;
import org.intelehealth.klivekit.utils.RtcUtilsKt;

import java.util.Objects;


public class CallRTCNotifyReceiver extends BroadcastReceiver {
    public static final String ACTION = "org.intelehealth.app.RTC_MESSAGE_EVENT";

    private static final String TAG = CallRTCNotifyReceiver.class.getName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG, "onReceive");
        if (intent != null && Objects.equals(intent.getAction(), ACTION) && intent.getExtras() != null) {
            Intent in = new Intent(context, HomeActivity.class);
            in.putExtras(intent.getExtras());
            in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(in);
        }
    }
}