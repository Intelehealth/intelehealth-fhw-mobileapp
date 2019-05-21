package io.intelehealth.client.services;

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.Build;
import android.support.annotation.RequiresApi;

import io.intelehealth.client.dao.PullDataDAO;
import io.intelehealth.client.utilities.Logger;
import io.intelehealth.client.utilities.ScheduleJobUtils;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class SyncJobService extends JobService {
    private static final String TAG = SyncJobService.class.getSimpleName();

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        PullDataDAO pullDataDAO = new PullDataDAO();
        pullDataDAO.pushDataApi();
        pullDataDAO.pullData(getApplicationContext());
        Logger.logD(TAG, "onStartjob");
        ScheduleJobUtils.scheduleJob(getApplicationContext());
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {

        return true;
    }
}
