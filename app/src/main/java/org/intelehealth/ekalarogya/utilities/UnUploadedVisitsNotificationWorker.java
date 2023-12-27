package org.intelehealth.ekalarogya.utilities;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.intelehealth.ekalarogya.BuildConfig;
import org.intelehealth.ekalarogya.R;
import org.intelehealth.ekalarogya.activities.activePatientsActivity.ActivePatientActivity;
import org.intelehealth.ekalarogya.activities.homeActivity.HomeActivity;
import org.intelehealth.ekalarogya.app.AppConstants;
import org.intelehealth.ekalarogya.webrtc.notification.AppNotification;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class UnUploadedVisitsNotificationWorker extends Worker {

    private final static String TAG = "UnUploadedVisitsNotificationWorker";
    private final SQLiteDatabase db;
    private final String channelId = "1";
    private final String channelName = "intelehealth";
    private final int mId = 1;
    private static boolean scheduled;

    public UnUploadedVisitsNotificationWorker(Context ctx, WorkerParameters params) {
        super(ctx, params);
        db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
    }

    @NonNull
    @Override
    public Result doWork() {
        if (getUnUploadedVisitCount(db) > 0) {
            showNotification(getApplicationContext().getString(R.string.title_un_uploaded_visit_reminder), getApplicationContext().getString(R.string.title_un_uploaded_visit_reminder_desc), getApplicationContext());
        }
        scheduled = false;
        schedule(getApplicationContext());
        return Result.success();
    }

    public static void schedule(Context context) {
        if (scheduled)
            return;
        Calendar currentDate = Calendar.getInstance();
        Calendar dueDate = Calendar.getInstance();
        int hourOfDay = currentDate.get(Calendar.HOUR_OF_DAY);
        int hour = 10;
        if (hourOfDay >= 10) {
            hour = 14;
        }
        if (hourOfDay >= 14) {
            hour = 17;
        }
        if (hourOfDay >= 17) {
            hour = 10;
        }
        dueDate.set(Calendar.HOUR_OF_DAY, hour);
        dueDate.set(Calendar.MINUTE, 0);
        dueDate.set(Calendar.SECOND, 0);
        if (dueDate.before(currentDate)) {
            dueDate.add(Calendar.HOUR_OF_DAY, 24);
        }
        long timeDiff = dueDate.getTimeInMillis() - currentDate.getTimeInMillis();
        if (BuildConfig.DEBUG) {
            timeDiff = TimeUnit.MINUTES.toMillis(5);
        }
        OneTimeWorkRequest dailyWorkRequest = new OneTimeWorkRequest.Builder(UnUploadedVisitsNotificationWorker.class)
                .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
                .addTag(TAG)
                .build();
        WorkManager.getInstance(context).enqueueUniqueWork(TAG, ExistingWorkPolicy.REPLACE, dailyWorkRequest);
        scheduled = true;
    }

    public int getUnUploadedVisitCount(SQLiteDatabase db) {
        int unUploadedVisitCount = 0;
        String query = "SELECT a.uuid, a.sync, a.patientuuid, a.startdate, a.enddate, b.first_name, " +
                "b.middle_name, b.last_name, b.date_of_birth, b.openmrs_id, b.gender " +
                "FROM tbl_visit a, tbl_patient b " +
                "WHERE a.patientuuid = b.uuid " +
                "AND a.voided = 0 AND a.enddate is NULL OR a.enddate='' GROUP BY a.uuid ORDER BY a.sync ASC ";
        final Cursor cursor = db.rawQuery(query, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    if (cursor.getString(cursor.getColumnIndexOrThrow("sync")).equalsIgnoreCase("0"))
                        unUploadedVisitCount++;
                } while (cursor.moveToNext());
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return unUploadedVisitCount;
    }

    public void showNotification(String title, String text, Context context) {
        NotificationManager mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(channelId, channelName, importance);
            mNotifyManager.createNotificationChannel(mChannel);
        }
//        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, channelId);
//        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, new Intent(context, ActivePatientActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
//        Notification notification = mBuilder
//                .setSmallIcon(R.drawable.ic_cloud_upload)
//                .setAutoCancel(true).setContentTitle(title)
//                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
//                .setContentIntent(contentIntent)
//                .setContentText(text).build();
//        mNotifyManager.notify(mId, mBuilder.build());
        new AppNotification.Builder(context)
                .pendingIntent(PendingIntent.getActivity(context, 0, new Intent(context, ActivePatientActivity.class), NotificationUtils.getPendingIntentFlag()))
                .title(title)
                .body(text)
                .send();
    }

}
