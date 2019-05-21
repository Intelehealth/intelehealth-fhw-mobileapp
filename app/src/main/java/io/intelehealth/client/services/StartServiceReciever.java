package io.intelehealth.client.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import io.intelehealth.client.utilities.ScheduleJobUtils;

public class StartServiceReciever extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        ScheduleJobUtils.scheduleJob(context);
    }
}
