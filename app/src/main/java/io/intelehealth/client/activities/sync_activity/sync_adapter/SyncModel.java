package io.intelehealth.client.activities.sync_activity.sync_adapter;

import android.database.Cursor;

import io.intelehealth.client.database.DelayedJobQueueProvider;

/**
 * Created by Dexter Barretto on 7/17/17.
 * Github : @dbarretto
 */

public class SyncModel {

    private String patientName;
    private String patientId;
    private String jobType;
    private Integer queueId;
    private Integer status;
    private Integer syncStatus;

    public SyncModel(Cursor cursor) {
        this.patientName = cursor.getString(cursor.getColumnIndexOrThrow(DelayedJobQueueProvider.PATIENT_NAME));
        this.patientId = cursor.getString(cursor.getColumnIndexOrThrow(DelayedJobQueueProvider.PATIENT_ID));
        this.jobType = cursor.getString(cursor.getColumnIndexOrThrow(DelayedJobQueueProvider.JOB_TYPE));
        this.syncStatus = cursor.getInt(cursor.getColumnIndexOrThrow(DelayedJobQueueProvider.SYNC_STATUS));
        this.status = cursor.getInt(cursor.getColumnIndexOrThrow(DelayedJobQueueProvider.STATUS));
        this.queueId = cursor.getInt(cursor.getColumnIndexOrThrow(DelayedJobQueueProvider._ID));
    }

    public SyncModel(String patientName, String patientId, String jobType, Integer queueId, Integer status, Integer syncStatus) {
        this.patientName = patientName;
        this.patientId = patientId;
        this.jobType = jobType;
        this.queueId = queueId;
        this.status = status;
        this.syncStatus = syncStatus;
    }

    public String getPatientName() {
        return patientName;
    }

    public String getPatientId() {
        return patientId;
    }

    public String getJobType() {
        return jobType;
    }

    public Integer getQueueId() {
        return queueId;
    }

    public Integer getStatus() {
        return status;
    }

    public Integer getSyncStatus() {
        return syncStatus;
    }
}
