package io.intelehealth.client.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ProgressCallback;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.intelehealth.client.R;
import io.intelehealth.client.database.DelayedJobQueueProvider;
import io.intelehealth.client.database.LocalRecordsDatabaseHelper;

import static io.intelehealth.client.services.ClientService.STATUS_SYNC_IN_PROGRESS;
import static io.intelehealth.client.services.ClientService.STATUS_SYNC_STOPPED;

/**
 * Created by Dexter Barretto on 6/9/17.
 * Github : @dbarretto
 */

public class PersonPhotoUploadService extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */

    private Bitmap bitmap;


    public PersonPhotoUploadService(String name) {
        super(name);
    }

    public PersonPhotoUploadService() {
        super(TAG);
    }

    NotificationManager mNotifyManager;
    NotificationCompat.Builder mBuilder;
    public int mId = 2;
    Integer patientId;

    Integer queueId = null;

    String imageName;
    String patientUUID;

    private static final String TAG = PersonPhotoUploadService.class.getSimpleName();

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        Log.i(TAG, "onHandleIntent: Photo upload Start");

        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this);

        if (!intent.hasExtra("queueId")) {
            int id = addJobToQueue(intent);
            intent.putExtra("queueId", id);
        }

        Log.d(TAG, "Queue id: " + intent.getIntExtra("queueId", -1));
        queueId = intent.getIntExtra("queueId", -1);
        patientUUID = intent.getStringExtra("patientUUID");
        patientId = intent.getIntExtra("patientID", -1);

        String query = "SELECT patient_photo FROM patient WHERE _id = ?";
        LocalRecordsDatabaseHelper databaseHelper = new LocalRecordsDatabaseHelper(this);
        SQLiteDatabase localdb = databaseHelper.getWritableDatabase();
        Cursor cursor = localdb.rawQuery(query, new String[]{String.valueOf(patientId)});
        List<String> imagePaths = new ArrayList<>();
        if (cursor.moveToFirst() && cursor.getCount() != 0) {
            do {
                imagePaths.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        localdb.close();
        if (!imagePaths.isEmpty()) {
            String filePath = imagePaths.get(0);
            if (filePath != null) {
                File profile_image = new File(filePath);
                imageName = profile_image.getName();
                imageName = imageName.replace('%', '_');

                if (profile_image != null) {
                    bitmap = BitmapFactory.decodeFile(filePath);
                }

                if (bitmap != null && patientUUID!=null && !patientUUID.isEmpty()) {
                    uploadImage("Profile", bitmap, imageName, intent);
                }
                else {
                    Log.d(TAG, "onHandleIntent: Error uploading image | Image or patient uuid is null");
                }
            } else {
                removeJobFromQueue(queueId);
            }
        }
    }

    public void uploadImage(String classname, Bitmap bitmap, final String imageName, final Intent intent) {
        queueSyncStart(queueId);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] image = stream.toByteArray();

        final ParseFile file = new ParseFile(imageName, image);
        ParseObject imgupload = new ParseObject(classname);
        imgupload.put("Image", file);
        imgupload.put("PatientID", patientUUID);

        file.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                Log.i(TAG, "done: "+  file.getUrl());
            }
        }, new ProgressCallback() {
            @Override
            public void done(Integer percentDone) {
                String newText = "Image Uploading.";
                mBuilder.setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(newText)
                        .setContentText(newText);
                mBuilder.setProgress(100,percentDone, false);
                mNotifyManager.notify(mId, mBuilder.build());
                Log.i(TAG, "done: " + percentDone);
            }
        });

        imgupload.saveInBackground(new SaveCallback() {
            @Override
            public void done(com.parse.ParseException e) {
                if (e == null) {
                    String newText = "Person Image Posted successfully.";
                    mBuilder.setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle("Image Upload")
                            .setContentText(newText);
                    mNotifyManager.notify(mId, mBuilder.build());
                    if (intent.hasExtra("queueId")) {
                        int queueId = intent.getIntExtra("queueId", -1);
                        removeJobFromQueue(queueId);
                    }
                } else {
                    String newText = "Failed to Post Images.";
                    mBuilder.setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle("Image Upload")
                            .setContentText(newText);
                    mNotifyManager.notify(mId, mBuilder.build());
                    if (queueId != null) {
                        queueSyncStop(queueId);
                    }
                }
            }
        });

    }


    private int addJobToQueue(Intent intent) {

        Log.d(TAG, "Adding to Queue");
        // Add a new Delayed Job record
        ContentValues values = new ContentValues();
        values.put(DelayedJobQueueProvider.JOB_TYPE, "photoUpload");
        values.put(DelayedJobQueueProvider.JOB_PRIORITY, 1);
        values.put(DelayedJobQueueProvider.JOB_REQUEST_CODE, 0);
        values.put(DelayedJobQueueProvider.PATIENT_ID, intent.getIntExtra("patientID", -1));
        values.put(DelayedJobQueueProvider.PATIENT_NAME, intent.getStringExtra("name"));
        values.put(DelayedJobQueueProvider.DATA_RESPONSE, intent.getStringExtra("patientUUID"));
        values.put(DelayedJobQueueProvider.SYNC_STATUS, 0);

        Uri uri = getContentResolver().insert(
                DelayedJobQueueProvider.CONTENT_URI, values);


        return Integer.valueOf(uri.getLastPathSegment());

    }

    private void removeJobFromQueue(int queueId) {
        Log.d(TAG, "Removing from Queue");
        if (queueId > -1) {
            String url = DelayedJobQueueProvider.URL + "/" + queueId;
            Uri uri = Uri.parse(url);
            int result = getContentResolver().delete(uri, null, null);
            if (result > 0) {
                Log.i(TAG, result + " row deleted");
            } else {
                Log.e(TAG, "Database error while deleting row!");
            }
        }

    }

    private void queueSyncStart(int queueId) {
        ContentValues values = new ContentValues();
        values.put(DelayedJobQueueProvider.SYNC_STATUS, STATUS_SYNC_IN_PROGRESS);
        String url = DelayedJobQueueProvider.URL + "/" + queueId;
        Uri uri = Uri.parse(url);
        getContentResolver().update(uri, values, null, null);
    }

    private void queueSyncStop(int queueId) {
        ContentValues values = new ContentValues();
        values.put(DelayedJobQueueProvider.SYNC_STATUS, STATUS_SYNC_STOPPED);
        String url = DelayedJobQueueProvider.URL + "/" + queueId;
        Uri uri = Uri.parse(url);
        int result = getContentResolver().update(uri, values, null, null);
    }

}
