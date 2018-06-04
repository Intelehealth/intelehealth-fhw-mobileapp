package io.intelehealth.client.activities.home_activity;

import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ProgressCallback;
import com.parse.SaveCallback;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import io.intelehealth.client.R;
import io.intelehealth.client.activities.setting_activity.SettingsActivity;
import io.intelehealth.client.database.DelayedJobQueueProvider;

/**
 * Created by Dexter Barretto on 3/6/18.
 * Github : @dbarretto
 */
public class BackupCloud {

    NotificationCompat.Builder mBuilder;

    private static final String TAG = BackupCloud.class.getSimpleName();

    private Context context;
    private Backup backup;
    SharedPreferences.Editor e;
    SharedPreferences sharedPreferences;
    NotificationManager mNotifyManager;
    public int mId = 4;
    String location;
    String user_id;

    public BackupCloud(Context context) {
        this.context = context;
        backup = getBackupInstance();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        e = sharedPreferences.edit();
        mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(context);
    }

    /**
     * @param queue_id Should be provided only if it task is fired by job queue service. If called by user interaction,it must be null
     */

    public void startCloudBackup(@Nullable Integer queue_id) {

        location = sharedPreferences.getString(SettingsActivity.KEY_PREF_LOCATION_NAME, null);
        user_id = sharedPreferences.getString("creatorid", null);

        if (queue_id == null) {
            queue_id = checkQueueforId();
        }

        if (!isNetworkAvailable()) {
            Toast.makeText(context, context.getString(R.string.no_network), Toast.LENGTH_SHORT).show();
            //Failed Task
            if (queue_id == null || queue_id.equals(-1)) {
                addJobToQueue();
            }
            return;
        }

        //get Local Backup first
        boolean check = getBackupInstance().checkDatabaseForData(context);
        if (check)
            e.putString("value", "yes");
        else
            e.putString("value", "no");

        e.apply();

        boolean backup_checker = false;
        try {
            backup_checker = backup.createFileInMemory(context);
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        //Contains logic to upload to cloud
        if (!isNetworkAvailable()) {
            Toast.makeText(context, context.getString(R.string.no_network), Toast.LENGTH_SHORT).show();
            //Failed Task
            if (queue_id == null || queue_id.equals(-1)) {
                addJobToQueue();
            }
            return;
        }

        if (!backup_checker) {
            Toast.makeText(context, context.getString(R.string.local_backup_failed), Toast.LENGTH_SHORT).show();
            //Failed Task
            if (queue_id == null || queue_id.equals(-1)) {
                addJobToQueue();
            }
            return;
        }

        //Upload to parse
        String newfilepath = Environment.getExternalStorageDirectory() + File.separator + "InteleHealth_DB" +
                File.separator + "Intelehealth.db"; // directory: Intelehealth_DB   ,  filename: Intelehealth.db
        Log.d("newfilepath", newfilepath);
        File myfile = new File(newfilepath);
        if (myfile.exists()) {
            uploadToParse(myfile, user_id, location, queue_id);
        }

        //Failed Task
        if (queue_id == null || queue_id.equals(-1)) {
            addJobToQueue();
        }

    }

    public void startCloudRestore() {
        //Check if backup exists locally
        location = sharedPreferences.getString(SettingsActivity.KEY_PREF_LOCATION_NAME, null);
        user_id = sharedPreferences.getString("creatorid", null);
        String local_backup_path = Environment.getExternalStorageDirectory() + File.separator + "InteleHealth_DB" +
                File.separator + "Intelehealth.db"; // directory: Intelehealth_DB   ,  filename: Intelehealth.db
        File local_backup = new File(local_backup_path);

        //Download Backup from cloud if not available locally
        if (!local_backup.exists()) {
            downloadFromParse(user_id, location);
        } else {
            Toast.makeText(context, R.string.local_backup_restore, Toast.LENGTH_SHORT).show();
        }
        try {
            Toast.makeText(context, context.getString(R.string.local_backup_restore), Toast.LENGTH_SHORT).show();
            backup.createFileInMemory(context);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    public void cloudRestoreForced() {
        //Download Backup from cloud
    }

    private Backup getBackupInstance() {
        if (backup == null) backup = new Backup();
        return backup;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private Integer checkQueueforId() {
        return 0;
    }

    private int getQueueId() {
        String SELECTION = DelayedJobQueueProvider.JOB_TYPE + "=";
        String[] ARGS = new String[]{"syncDB"};
        Cursor cursor = context.getContentResolver().query(DelayedJobQueueProvider.CONTENT_URI, null, SELECTION, ARGS, null);
        if (cursor != null && cursor.moveToFirst() && cursor.getCount() > 0) {
            return cursor.getColumnIndex(DelayedJobQueueProvider._ID);
        } else return -1;
    }

    private int addJobToQueue() {
        Log.d(TAG, "Adding to Queue");
        String serviceCall = "syncDB";
        // Add a new Delayed Job record
        ContentValues values = new ContentValues();
        values.put(DelayedJobQueueProvider.JOB_TYPE, serviceCall);
        values.put(DelayedJobQueueProvider.JOB_PRIORITY, 1);
        values.put(DelayedJobQueueProvider.JOB_REQUEST_CODE, 0);
        values.put(DelayedJobQueueProvider.PATIENT_NAME, "Intelehealth");
        values.put(DelayedJobQueueProvider.PATIENT_ID, 0);
        values.put(DelayedJobQueueProvider.SYNC_STATUS, 0);

        Uri uri = context.getContentResolver().insert(
                DelayedJobQueueProvider.CONTENT_URI, values);

        return Integer.valueOf(uri.getLastPathSegment());

    }

    private void removeJobFromQueue(Integer queueId) {
        Log.d(TAG, "Removing from Queue");
        if (queueId != null && queueId > -1) {
            String url = DelayedJobQueueProvider.URL + "/" + queueId;
            Uri uri = Uri.parse(url);
            int result = context.getContentResolver().delete(uri, null, null);
            if (result > 0) {
                Log.i(TAG, result + " row deleted");
            } else {
                Log.e(TAG, "Database error while deleting row!");

            }
        }
    }

    private void uploadToParse(File database, String user_id, String location, final Integer queue_id) {
        if (user_id != null && location != null) {
            final ParseFile db_file = new ParseFile(database);
            final ParseObject db_upload = new ParseObject("BackupDatabase");
            db_upload.put("db", db_file);
            db_upload.put("user_id", user_id);
            db_upload.put("location", location);

            db_file.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null) {
                        //Failed Task
                        if (queue_id == null || queue_id.equals(-1)) {
                            addJobToQueue();
                        }
                    }

                }
            }, new ProgressCallback() {
                @Override
                public void done(Integer percentDone) {
                    String newText = "Database Uploading - " + percentDone + "%";
                    mBuilder.setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle(newText)
                            .setContentText(newText);
                    mBuilder.setProgress(100, percentDone, false);
                    mNotifyManager.notify(mId, mBuilder.build());
                    Log.i(TAG, "done: " + percentDone);
                }
            });

            db_upload.saveInBackground(new SaveCallback() {
                @Override
                public void done(com.parse.ParseException e) {
                    if (e == null) {
                        String newText = "Database Upload Complete";
                        mBuilder.setSmallIcon(R.mipmap.ic_launcher)
                                .setContentTitle("Image Upload")
                                .setContentText(newText);
                        mNotifyManager.notify(mId, mBuilder.build());
                        removeJobFromQueue(queue_id);
                        //Success
                        if (queue_id != null && !queue_id.equals(-1)) {
                            removeJobFromQueue(queue_id);
                        }
                    } else {
                        String newText = "Database Upload Failed";
                        mBuilder.setSmallIcon(R.mipmap.ic_launcher)
                                .setContentTitle("Image Upload")
                                .setContentText(newText);
                        mNotifyManager.notify(mId, mBuilder.build());
                        //Failed Task
                        if (queue_id == null || queue_id.equals(-1)) {
                            addJobToQueue();
                        }

                    }
                }
            });
        }
    }

    private void downloadFromParse(String user_id, String location) {
        if (user_id != null && location != null) {
            ParseQuery<ParseObject> query = ParseQuery.getQuery("BackupDatabase");
            query.whereEqualTo("user_id", user_id);
            query.whereEqualTo("location", location);
            query.getFirstInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject object, ParseException e) {
                    if (object == null) {
                        Toast.makeText(context, "Database backup not available", Toast.LENGTH_SHORT).show();
                    } else {
                        final ParseFile file = (ParseFile) object.get("db");
                        file.getDataInBackground(new GetDataCallback() {
                            @Override
                            public void done(byte[] data, ParseException e) {
                                File myDir = new File(Environment.getExternalStorageDirectory() + File.separator + "InteleHealth_DB");
                                if (myDir.exists()) {
                                } else {
                                    myDir.mkdir();
                                }
                                //file created inside internal memory, outside app package, doesnt delete if app is uninstalled
                                String newfilepath = Environment.getExternalStorageDirectory() + File.separator + "InteleHealth_DB" +
                                        File.separator + "Intelehealth.db"; // directory: Intelehealth_DB   ,  filename: Intelehealth.db
                                Log.d("newfilepath", newfilepath);
                                File myfile = new File(newfilepath);
                                if (myfile.exists()) myfile.delete();
                                try {
                                    myfile.createNewFile();
                                    FileOutputStream fileOutputStream = new FileOutputStream(myfile);
                                    fileOutputStream.write(data);
                                    fileOutputStream.close();
                                } catch (FileNotFoundException exfnf) {
                                } catch (IOException exio) {
                                    Toast.makeText(context, "Error writing backup file", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
            });
        }

    }
}
