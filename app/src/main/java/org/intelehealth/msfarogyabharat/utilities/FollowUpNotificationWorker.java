package org.intelehealth.msfarogyabharat.utilities;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.intelehealth.msfarogyabharat.BuildConfig;
import org.intelehealth.msfarogyabharat.R;
import org.intelehealth.msfarogyabharat.activities.homeActivity.HomeActivity;
import org.intelehealth.msfarogyabharat.app.AppConstants;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class FollowUpNotificationWorker extends Worker {
    private final static String TAG = "FollowUpNotificationWorker";
    private final SQLiteDatabase db;
    private final String channelId = "1";
    private final String channelName = "intelehealth";
    private final int mId = 1;
    private static boolean scheduled;

    public FollowUpNotificationWorker(Context ctx, WorkerParameters params) {
        super(ctx, params);
        db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
    }

    @NonNull
    @Override
    public Result doWork() {
        if (getFollowUpCount(db) > 0) {
            showNotification(getApplicationContext().getString(R.string.title_follow_reminder), getApplicationContext().getString(R.string.title_follow_up_reminder_desc), getApplicationContext());
        }
        scheduled = false;
        schedule();
        return Result.success();
    }

    public static void schedule() {
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
//        WorkManager.getInstance().cancelAllWorkByTag(TAG);
        OneTimeWorkRequest dailyWorkRequest = new OneTimeWorkRequest.Builder(FollowUpNotificationWorker.class)
                .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
                .addTag(TAG)
                .build();
        WorkManager.getInstance().enqueueUniqueWork(TAG, ExistingWorkPolicy.REPLACE, dailyWorkRequest);
        scheduled = true;
    }

    public static long getFollowUpCount(SQLiteDatabase db) {
        int count = 0;
        Date cDate = new Date();
        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(cDate);
        String currentDate_new = new SimpleDateFormat("dd/MMM/yyyy", Locale.ENGLISH).format(cDate);
        String query = "SELECT * FROM tbl_patient as p where p.uuid in (select v.patientuuid from tbl_visit as v where v.uuid in (select e.visituuid from tbl_encounter as e where e.uuid in (select o.encounteruuid from tbl_obs as o where (o.conceptuuid = ? and o.value like '%" + currentDate_new + "%') or (o.conceptuuid = ? and o.value like '%" + currentDate + "%'))))";
        final Cursor cursor = db.rawQuery(query, new String[]{UuidDictionary.CONCEPT_RESOLUTION, UuidDictionary.FOLLOW_UP_VISIT});
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    count++;
                } while (cursor.moveToNext());
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return count;
    }

    private static String getSeverity(String patientUid, SQLiteDatabase db) {
        String severity = null;
        final Cursor obsCursor = db.rawQuery("select o.value from tbl_obs as o where o.conceptuuid = ? and encounteruuid in (select e.uuid from tbl_encounter as e where e.visituuid in (select v.uuid from tbl_visit as v where v.patientuuid = ?))", new String[]{UuidDictionary.PHYSICAL_EXAMINATION, patientUid});
        if (obsCursor.moveToFirst()) {
            do {
                severity = obsCursor.getString(obsCursor.getColumnIndexOrThrow("value"));
            } while (obsCursor.moveToNext());
            obsCursor.close();
        }
        return severity;
    }

    public void showNotification(String title, String text, Context context) {
        NotificationManager mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        //mahiti added
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(channelId, channelName, importance);
            mNotifyManager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, channelId);
        PendingIntent contentIntent;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            contentIntent = PendingIntent.getActivity(context, 0, new Intent(context, HomeActivity.class), PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        } else {
            contentIntent = PendingIntent.getActivity(context, 0, new Intent(context, HomeActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
        }

        Notification notification = mBuilder
                .setSmallIcon(R.drawable.ic_cloud_upload)
                .setAutoCancel(true).setContentTitle(title)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setContentIntent(contentIntent)
                .setContentText(text).build();
        mNotifyManager.notify(mId, mBuilder.build());

    }
}
