package io.intelehealth.client.utilities;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;

import io.intelehealth.client.app.AppConstants;
import io.intelehealth.client.services.SyncJobService;

public class ScheduleJobUtils {

    // schedule the start of the service every 10 - 30 seconds
    public static void scheduleJob(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ComponentName serviceComponent = new ComponentName(context, SyncJobService.class);
            JobInfo.Builder builder = new JobInfo.Builder(0, serviceComponent);
            builder.setMinimumLatency(AppConstants.SYNC); // wait at least
            builder.setOverrideDeadline(AppConstants.MAXIMUM_DELAY); // maximum delay
            //builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED); // require unmetered network
            //builder.setRequiresDeviceIdle(true); // device should be idle
            //builder.setRequiresCharging(false); // we don't care if the device is charging or not
            JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
            jobScheduler.schedule(builder.build());
        } else {

        }
    }

}
