package org.intelehealth.app.utilities;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.intelehealth.app.BuildConfig;
import org.intelehealth.app.R;
import org.intelehealth.app.activities.homeActivity.HomeActivity;
import org.intelehealth.app.app.AppConstants;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class FollowUpNotificationWorker  extends Worker {

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
        String query = "SELECT a.uuid, a.sync, a.patientuuid, a.startdate, a.enddate, b.uuid, b.first_name, b.middle_name, b.last_name, b.date_of_birth, b.openmrs_id, c.value AS speciality, o.value FROM tbl_visit a, tbl_patient b, tbl_encounter d, tbl_obs o, tbl_visit_attribute c WHERE a.uuid = c.visit_uuid AND  a.enddate is NOT NULL AND a.patientuuid = b.uuid AND a.uuid = d.visituuid AND d.uuid = o.encounteruuid AND o.conceptuuid = ? AND o.value is NOT NULL GROUP BY a.patientuuid";
        final Cursor cursor = db.rawQuery(query, new String[]{UuidDictionary.FOLLOW_UP_VISIT});
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    try {
                        String followUpDate = cursor.getString(cursor.getColumnIndexOrThrow("value")).substring(0, 10);
                        Date followUp = new SimpleDateFormat("dd-MM-yyyy").parse(followUpDate);
                        Date currentD = new SimpleDateFormat("dd-MM-yyyy").parse(currentDate);
                        int value = followUp.compareTo(currentD);
                        if (value == -1 || value == 0) {
                            count++;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } while (cursor.moveToNext());
            }
            if (cursor != null) {
                cursor.close();
            }
        }
        return count;
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
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, new Intent(context, HomeActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = mBuilder
                .setSmallIcon(R.drawable.ic_cloud_upload)
                .setAutoCancel(true).setContentTitle(title)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setContentIntent(contentIntent)
                .setContentText(text).build();
        mNotifyManager.notify(mId, mBuilder.build());
    }
}
