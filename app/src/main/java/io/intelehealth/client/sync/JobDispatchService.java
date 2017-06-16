package io.intelehealth.client.sync;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

import io.intelehealth.client.db.DelayedJobQueueProvider;
import io.intelehealth.client.services.ClientService;

/**
 * Start {@link ClientService} when online to upload the queued data.
 * <p>
 * Created by Dexter Barretto on 5/31/17.
 * Github : @dbarretto
 */

public class JobDispatchService extends JobService {

    String URL = "io.intelehealth.client.db.DelayedJobQueueProvider";
    public static final String LOG_TAG = JobDispatchService.class.getSimpleName();

    @Override
    public boolean onStartJob(JobParameters job) {

        String[] DELAYED_JOBS_PROJECTION = new String[]{DelayedJobQueueProvider._ID,
                DelayedJobQueueProvider.JOB_TYPE,
                DelayedJobQueueProvider.JOB_PRIORITY,
                DelayedJobQueueProvider.JOB_REQUEST_CODE,
                DelayedJobQueueProvider.PATIENT_NAME,
                DelayedJobQueueProvider.PATIENT_ID,
                DelayedJobQueueProvider.VISIT_ID,
                DelayedJobQueueProvider.VISIT_UUID};

        Uri jobs_uri = Uri.parse(URL);
        Cursor c = getContentResolver().query(jobs_uri, null, null, null, null);

        if (c.moveToFirst()) {
            do {
                String service_call = c.getString(c.getColumnIndex(DelayedJobQueueProvider.JOB_TYPE));
                Intent serviceIntent = new Intent(this, ClientService.class);
                serviceIntent.putExtra("queueId", c.getInt(c.getColumnIndex(DelayedJobQueueProvider._ID)));
                switch (service_call) {
                    case "patient": {
                        serviceIntent.putExtra("serviceCall", service_call);
                        serviceIntent.putExtra("patientID", c.getString(c.getColumnIndex(DelayedJobQueueProvider.PATIENT_ID)));
                        serviceIntent.putExtra("name", c.getString(c.getColumnIndex(DelayedJobQueueProvider.PATIENT_NAME)));
                        serviceIntent.putExtra("status", c.getInt(c.getColumnIndex(DelayedJobQueueProvider.STATUS)));
                        serviceIntent.putExtra("personResponse", c.getInt(c.getColumnIndex(DelayedJobQueueProvider.DATA_RESPONSE)));
                        break;
                    }
                    case "visit": {
                        serviceIntent.putExtra("serviceCall", service_call);
                        serviceIntent.putExtra("patientID", c.getString(c.getColumnIndex(DelayedJobQueueProvider.PATIENT_ID)));
                        serviceIntent.putExtra("name", c.getString(c.getColumnIndex(DelayedJobQueueProvider.PATIENT_NAME)));
                        serviceIntent.putExtra("visitID", c.getString(c.getColumnIndex(DelayedJobQueueProvider.VISIT_ID)));
                        serviceIntent.putExtra("status", c.getInt(c.getColumnIndex(DelayedJobQueueProvider.STATUS)));
                        serviceIntent.putExtra("visitResponse", c.getInt(c.getColumnIndex(DelayedJobQueueProvider.DATA_RESPONSE)));
                        break;
                    }
                    case "endVisit": {
                        serviceIntent.putExtra("serviceCall", service_call);
                        serviceIntent.putExtra("patientID", c.getString(c.getColumnIndex(DelayedJobQueueProvider.PATIENT_ID)));
                        serviceIntent.putExtra("name", c.getString(c.getColumnIndex(DelayedJobQueueProvider.PATIENT_NAME)));
                        serviceIntent.putExtra("visitUUID", c.getString(c.getColumnIndex(DelayedJobQueueProvider.VISIT_UUID)));
                        break;
                    }
                    case "photoUpload": {
                        serviceIntent.putExtra("serviceCall", service_call);
                        serviceIntent.putExtra("patientID", c.getString(c.getColumnIndex(DelayedJobQueueProvider.PATIENT_ID)));
                        serviceIntent.putExtra("name", c.getString(c.getColumnIndex(DelayedJobQueueProvider.PATIENT_NAME)));
                        serviceIntent.putExtra("person", c.getInt(c.getColumnIndex(DelayedJobQueueProvider.DATA_RESPONSE)));
                        break;
                    }
                    default:
                        Log.e(LOG_TAG, "Does not match any Job Type");
                }
                startService(serviceIntent);
            } while (c.moveToNext());
        }
        return false; // Answers the question: "Is there still work going on?"
    }

    @Override
    public boolean onStopJob(JobParameters job) {

        return false; // Answers the question: "Should this job be retried?"
    }
}