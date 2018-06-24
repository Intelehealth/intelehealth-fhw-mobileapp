package io.intelehealth.client.activities.home_activity;

import android.app.Dialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
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
import io.intelehealth.client.services.ImageDownloadService;

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

    private Dialog dialog;

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

        if (queue_id == null) {
            dialog = new ProgressDialog(context);
            dialog.setTitle(context.getString(R.string.cloud_upload_header));
            dialog.setCancelable(false);
            dialog.show();
        }

        location = sharedPreferences.getString(SettingsActivity.KEY_PREF_LOCATION_NAME, null);
        user_id = sharedPreferences.getString("creatorid", null);

        if (queue_id == null) {
            queue_id = getQueueId();
        }

        //get Local Backup first
        boolean check = getBackupInstance().checkDatabaseForData(context);

        if (!check) {
            Toast.makeText(context, context.getString(R.string.no_data), Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            return;
        }

        boolean backup_checker = false;
        try {
            backup_checker = backup.createFileInMemory(context, true);
            dialog.dismiss();
        } catch (IOException e1) {
            dialog.dismiss();
            e1.printStackTrace();
        }

        //Contains logic to upload to cloud
        if (!isNetworkAvailable()) {
            Toast.makeText(context, context.getString(R.string.no_network), Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            //Failed Task
            if (queue_id == null || queue_id.equals(-1)) {
                addJobToQueue();
            }
            return;
        }

        if (!backup_checker) {
            Toast.makeText(context, context.getString(R.string.local_backup_failed), Toast.LENGTH_SHORT).show();
            dialog.dismiss();
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
        dialog = new ProgressDialog(context);
        dialog.setTitle(context.getString(R.string.restore_data));
        dialog.setCancelable(false);
        dialog.show();
        boolean check = getBackupInstance().checkDatabaseForData(context);
        if (check) {
            dialog.dismiss();
            Toast.makeText(context, context.getString(R.string.error_existing_data), Toast.LENGTH_SHORT).show();
            return;
        }
        //Check if backup exists locally
        location = sharedPreferences.getString(SettingsActivity.KEY_PREF_LOCATION_NAME, null);
        user_id = sharedPreferences.getString("creatorid", null);
        String local_backup_path = Environment.getExternalStorageDirectory() + File.separator + "InteleHealth_DB" +
                File.separator + "Intelehealth.db"; // directory: Intelehealth_DB   ,  filename: Intelehealth.db
        File local_backup = new File(local_backup_path);

        //Download Backup from cloud if not available locally
        if (!local_backup.exists()) {
            Toast.makeText(context, context.getString(R.string.cloud_download_data), Toast.LENGTH_SHORT).show();
            if (isNetworkAvailable()) downloadFromParse(user_id, location);
            else {
                dialog.dismiss();
                Toast.makeText(context, context.getString(R.string.no_network), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, R.string.local_backup_restore, Toast.LENGTH_SHORT).show();
            try {
                boolean isSuccess = backup.createFileInMemory(context, false);
                if (isSuccess) {
                    Intent serviceIntent = new Intent(context, ImageDownloadService.class);
                    context.startService(serviceIntent);
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            dialog.dismiss();
        }

    }

    public void cloudRestoreForced() {
        //Download Backup from cloud
        dialog = new ProgressDialog(context);
        dialog.setTitle(context.getString(R.string.force_restore_header));
        dialog.setCancelable(false);
        dialog.show();
        location = sharedPreferences.getString(SettingsActivity.KEY_PREF_LOCATION_NAME, null);
        user_id = sharedPreferences.getString("creatorid", null);
        if (isNetworkAvailable()) downloadFromParse(user_id, location);
        else {
            dialog.dismiss();
            Toast.makeText(context, context.getString(R.string.no_network), Toast.LENGTH_SHORT).show();
        }
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

    private int getQueueId() {
        String SELECTION = DelayedJobQueueProvider.JOB_TYPE + "=?";
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
                    String newText = context.getString(R.string.database_uploading)+" - " + percentDone + "%";
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
                        mBuilder.setSmallIcon(R.mipmap.ic_launcher)
                                .setContentTitle(context.getString(R.string.database_upload))
                                .setContentText(context.getString(R.string.database_upload_complete));
                        mNotifyManager.notify(mId, mBuilder.build());
                        removeJobFromQueue(queue_id);
                        dialog.dismiss();
                        //Success
                        if (queue_id != null && !queue_id.equals(-1)) {
                            removeJobFromQueue(queue_id);
                        }
                    } else {
                        mBuilder.setSmallIcon(R.mipmap.ic_launcher)
                                .setContentTitle(context.getString(R.string.database_upload))
                                .setContentText(context.getString(R.string.database_upload_failed));
                        mNotifyManager.notify(mId, mBuilder.build());
                        dialog.dismiss();
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
            query.whereEqualTo("location", location);
            query.getFirstInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject object, ParseException e) {
                    if (object == null) {
                        dialog.dismiss();
                        Toast.makeText(context, context.getString(R.string.db_backup_unavailable), Toast.LENGTH_SHORT).show();
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
                                    boolean isSuccess = backup.createFileInMemory(context, false);
                                    if (isSuccess) {
                                        Intent serviceIntent = new Intent(context, ImageDownloadService.class);
                                        context.getApplicationContext().startService(serviceIntent);
                                    }

                                } catch (FileNotFoundException exfnf) {
                                } catch (IOException exio) {
                                    Toast.makeText(context, context.getString(R.string.db_file_write_error), Toast.LENGTH_SHORT).show();
                                } finally {
                                    dialog.dismiss();
                                }
                            }
                        });
                    }
                }
            });
        }

    }
}

