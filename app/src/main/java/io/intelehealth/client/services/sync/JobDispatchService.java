package io.intelehealth.client.services.sync;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

import io.intelehealth.client.database.DelayedJobQueueProvider;
import io.intelehealth.client.services.ClientService;
import io.intelehealth.client.services.ImageUploadService;
import io.intelehealth.client.services.PatientUpdateService;
import io.intelehealth.client.services.PersonPhotoUploadService;
import io.intelehealth.client.services.PrescriptionDownloadService;

/**
 * Start {@link ClientService} when online to upload the queued data.
 * <p>
 * Created by Dexter Barretto on 5/31/17.
 * Github : @dbarretto
 */

public class JobDispatchService extends JobService {

    String URL = "io.intelehealth.client.database.DelayedJobQueueProvider";
    public static final String LOG_TAG = JobDispatchService.class.getSimpleName();

    @Override
    public boolean onStartJob(JobParameters job) {

        Uri jobs_uri = Uri.parse(URL);
        Cursor cursor = getContentResolver().query(jobs_uri, null, null, null, null);
        if ((cursor != null) && (cursor.getCount() > 0)) {
            if (cursor.moveToFirst()) {
                do {
                    String service_call = cursor.getString(cursor.getColumnIndex(DelayedJobQueueProvider.JOB_TYPE));
                    Intent serviceIntent = null;

                    int sync_status = cursor.getInt(cursor.getColumnIndex(DelayedJobQueueProvider.SYNC_STATUS));
                    switch (service_call) {
                        case "patient": {
                            serviceIntent = new Intent(this, ClientService.class);
                            serviceIntent.putExtra("serviceCall", service_call);
                            serviceIntent.putExtra("patientID", cursor.getString(cursor.getColumnIndex(DelayedJobQueueProvider.PATIENT_ID)));
                            serviceIntent.putExtra("name", cursor.getString(cursor.getColumnIndex(DelayedJobQueueProvider.PATIENT_NAME)));
                            serviceIntent.putExtra("status", cursor.getInt(cursor.getColumnIndex(DelayedJobQueueProvider.STATUS)));
                            serviceIntent.putExtra("personResponse", cursor.getInt(cursor.getColumnIndex(DelayedJobQueueProvider.DATA_RESPONSE)));
                            break;
                        }
                        case "patientUpdate": {
                            serviceIntent = new Intent(this, PatientUpdateService.class);
                            serviceIntent.putExtra("serviceCall", service_call);
                            serviceIntent.putExtra("patientID", cursor.getString(cursor.getColumnIndex(DelayedJobQueueProvider.PATIENT_ID)));
                            serviceIntent.putExtra("name", cursor.getString(cursor.getColumnIndex(DelayedJobQueueProvider.PATIENT_NAME)));
                            break;
                        }
                        case "visit": {
                            serviceIntent = new Intent(this, ClientService.class);
                            serviceIntent.putExtra("serviceCall", service_call);
                            serviceIntent.putExtra("patientID", cursor.getString(cursor.getColumnIndex(DelayedJobQueueProvider.PATIENT_ID)));
                            serviceIntent.putExtra("name", cursor.getString(cursor.getColumnIndex(DelayedJobQueueProvider.PATIENT_NAME)));
                            serviceIntent.putExtra("visitID", cursor.getString(cursor.getColumnIndex(DelayedJobQueueProvider.VISIT_ID)));
                            serviceIntent.putExtra("status", cursor.getInt(cursor.getColumnIndex(DelayedJobQueueProvider.STATUS)));
                            serviceIntent.putExtra("visitResponse", cursor.getInt(cursor.getColumnIndex(DelayedJobQueueProvider.DATA_RESPONSE)));
                            break;
                        }
                        case "endVisit": {
                            serviceIntent = new Intent(this, ClientService.class);
                            serviceIntent.putExtra("serviceCall", service_call);
                            serviceIntent.putExtra("patientID", cursor.getString(cursor.getColumnIndex(DelayedJobQueueProvider.PATIENT_ID)));
                            serviceIntent.putExtra("name", cursor.getString(cursor.getColumnIndex(DelayedJobQueueProvider.PATIENT_NAME)));
                            serviceIntent.putExtra("visitUUID", cursor.getString(cursor.getColumnIndex(DelayedJobQueueProvider.VISIT_UUID)));
                            break;
                        }
                        case "photoUpload": {
                            serviceIntent = new Intent(this, PersonPhotoUploadService.class);
                            serviceIntent.putExtra("serviceCall", service_call);
                            serviceIntent.putExtra("patientID", cursor.getString(cursor.getColumnIndex(DelayedJobQueueProvider.PATIENT_ID)));
                            serviceIntent.putExtra("name", cursor.getString(cursor.getColumnIndex(DelayedJobQueueProvider.PATIENT_NAME)));
                            serviceIntent.putExtra("patientUUID", cursor.getInt(cursor.getColumnIndex(DelayedJobQueueProvider.DATA_RESPONSE)));
                            break;
                        }
                        case "imageUpload": {
                            serviceIntent = new Intent(this, ImageUploadService.class);
                            serviceIntent.putExtra("serviceCall", service_call);
                            serviceIntent.putExtra("patientID", cursor.getString(cursor.getColumnIndex(DelayedJobQueueProvider.PATIENT_ID)));
                            serviceIntent.putExtra("name", cursor.getString(cursor.getColumnIndex(DelayedJobQueueProvider.PATIENT_NAME)));
                            serviceIntent.putExtra("visitUUID", cursor.getString(cursor.getColumnIndex(DelayedJobQueueProvider.VISIT_UUID)));
                            serviceIntent.putExtra("patientUUID", cursor.getInt(cursor.getColumnIndex(DelayedJobQueueProvider.DATA_RESPONSE)));
                            break;
                        }
                        case"prescriptionDownload":{
                            serviceIntent = new Intent(this, PrescriptionDownloadService.class);
                            serviceIntent.putExtra("serviceCall", service_call);
                            serviceIntent.putExtra("patientID", cursor.getString(cursor.getColumnIndex(DelayedJobQueueProvider.PATIENT_ID)));
                            serviceIntent.putExtra("visitID", cursor.getString(cursor.getColumnIndex(DelayedJobQueueProvider.VISIT_ID)));
                            serviceIntent.putExtra("name", cursor.getString(cursor.getColumnIndex(DelayedJobQueueProvider.PATIENT_NAME)));
                            serviceIntent.putExtra("visitUUID", cursor.getString(cursor.getColumnIndex(DelayedJobQueueProvider.VISIT_UUID)));
                            break;
                        }
                        case"obsUpdate":{
                            serviceIntent = new Intent(this, PrescriptionDownloadService.class);
                            serviceIntent.putExtra("serviceCall", service_call);
                            serviceIntent.putExtra("patientID", cursor.getString(cursor.getColumnIndex(DelayedJobQueueProvider.PATIENT_ID)));
                            serviceIntent.putExtra("visitID", cursor.getString(cursor.getColumnIndex(DelayedJobQueueProvider.VISIT_ID)));
                            serviceIntent.putExtra("name", cursor.getString(cursor.getColumnIndex(DelayedJobQueueProvider.PATIENT_NAME)));
                            break;
                        }
                        default:
                            Log.e(LOG_TAG, "Does not match any Job Type");
                    }
                    if (serviceIntent != null) {
                        serviceIntent.putExtra("queueId", cursor.getInt(cursor.getColumnIndex(DelayedJobQueueProvider._ID)));
                        startService(serviceIntent);
                    }
                } while (cursor.moveToNext());
            }
        }
        if((cursor != null)) cursor.close();
        return false; // Answers the question: "Is there still work going on?"
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        return false; // Answers the question: "Should this job be retried?"
    }
}