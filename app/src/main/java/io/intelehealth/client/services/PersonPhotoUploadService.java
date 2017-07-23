package io.intelehealth.client.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ParseException;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;

import io.intelehealth.client.R;
import io.intelehealth.client.database.LocalRecordsDatabaseHelper;
import io.intelehealth.client.utilities.HelperMethods;
import io.intelehealth.client.database.DelayedJobQueueProvider;
import io.intelehealth.client.objects.WebResponse;

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

    public PersonPhotoUploadService() {super("PersonPhotoUploadService");}
    public PersonPhotoUploadService(String name) {
        super(name);
    }

    private static final String LOG_TAG = PersonPhotoUploadService.class.getSimpleName();

    NotificationManager mNotifyManager;
    NotificationCompat.Builder mBuilder;
    public int mId = 2;
    String patientId, visitId;

    String imageName;

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this);

        patientId = intent.getStringExtra("patientID");
        String person = intent.getStringExtra("person");
        visitId = intent.getStringExtra("visitID");

        String base64EncodedImage = null;

        String filePath = getExternalFilesDir(Environment.DIRECTORY_PICTURES) + File.separator +
                "Patient_Images" + File.separator + patientId;

        File profile_image = new File(filePath);
        File[] files = profile_image.listFiles();

        for (File file : files) {
            if (file.isFile()) {
                String name = file.getName();
                if(name.endsWith(".jpg"))
                    imageName = name;
            }
        }
        filePath += File.separator +  imageName;

        if (profile_image != null) {
            bitmap = BitmapFactory.decodeFile(filePath);

           // uploadImage(classname);
        }

        if (bitmap!=null){//base64EncodedImage != null) {
            byte[] byteArray = bitmapToByteArray(bitmap);
            base64EncodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);
            String photoString =
                    String.format("{\"person\":\"%s\"," +
                                    "\"base64EncodedImage\":\"%s\"}",
                            person,
                            base64EncodedImage);

            WebResponse responsePersonImage;
            responsePersonImage = HelperMethods.postCommand("personimage", photoString, getApplicationContext());

            if (responsePersonImage != null && responsePersonImage.getResponseCode() != 200) {
                String newText = "Person Image posting unsuccessful";
                mBuilder.setContentText(newText)
                        .setContentTitle("Profile Image Upload")
                        .setSmallIcon(R.mipmap.ic_launcher);
                mNotifyManager.notify(mId, mBuilder.build());
                addJobToQueue(intent);
                Log.d(LOG_TAG, "Person Image Posting Unsuccessful");

            } else if (responsePersonImage == null) {
                addJobToQueue(intent);
                Log.d(LOG_TAG, "Person Image Posting unsuccessful");

            } else {
                uploadImage("Profile",bitmap,imageName,intent,filePath);
            }
        }
    }

    public void uploadImage(String classname, Bitmap bitmap, final String imageName, final Intent intent, final String imagePath) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] image = stream.toByteArray();
        ParseFile file = new ParseFile(imageName, image);
        ParseObject imgupload = new ParseObject(classname);
        imgupload.put("Image", file);
        imgupload.put("PatientID", patientId);
        imgupload.saveInBackground(new SaveCallback() {
            @Override
            public void done(com.parse.ParseException e) {
                if (e == null) {
                    String newText = "Person Profile Image Posted successfully.";
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
                    addJobToQueue(intent);
                }
            }
        });
    }

    private byte[] bitmapToByteArray(Bitmap image) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }


    private void addJobToQueue(Intent intent) {
        if (!intent.hasExtra("queueId")) {
            Log.d(LOG_TAG, "Adding to Queue");
            // Add a new Delayed Job record
            ContentValues values = new ContentValues();
            values.put(DelayedJobQueueProvider.JOB_TYPE, "photoUpload");
            values.put(DelayedJobQueueProvider.PATIENT_NAME, intent.getStringExtra("name"));
            values.put(DelayedJobQueueProvider.JOB_PRIORITY, 1);
            values.put(DelayedJobQueueProvider.JOB_REQUEST_CODE, 0);
            values.put(DelayedJobQueueProvider.PATIENT_ID, intent.getStringExtra("patientID"));
            values.put(DelayedJobQueueProvider.DATA_RESPONSE, intent.getStringExtra("person"));
        }
    }

    private void removeJobFromQueue(int queueId) {
        Log.d(LOG_TAG, "Removing from Queue");
        if (queueId > -1) {
            String url = DelayedJobQueueProvider.URL + "/" + queueId;
            Uri uri = Uri.parse(url);
            int result = getContentResolver().delete(uri, null, null);
            if (result > 0) {
                Log.i(LOG_TAG, result + " row deleted");
            } else {
                Log.e(LOG_TAG, "Database error while deleting row!");
            }
        }

    }
}
