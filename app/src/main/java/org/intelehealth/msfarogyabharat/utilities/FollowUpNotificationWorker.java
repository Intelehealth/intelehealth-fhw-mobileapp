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
import android.util.Log;

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
import org.intelehealth.msfarogyabharat.models.FollowUpModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.intelehealth.msfarogyabharat.utilities.DateAndTimeUtils.mGetDaysAccording;

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
        Executors.newSingleThreadExecutor().execute(() -> {
            // Todo: Temporary deleted
            long count = getFollowUpCount();
            if (count > 0) {
                showNotification(getApplicationContext().getString(R.string.title_follow_reminder), getApplicationContext().getString(R.string.title_follow_up_reminder_desc), getApplicationContext());
            }
            scheduled = false;
            schedule();
        });
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
          //  timeDiff = TimeUnit.MINUTES.toMillis(5);
        }
//        WorkManager.getInstance().cancelAllWorkByTag(TAG);
        OneTimeWorkRequest dailyWorkRequest = new OneTimeWorkRequest.Builder(FollowUpNotificationWorker.class)
                .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
                .addTag(TAG)
                .build();
        WorkManager.getInstance().enqueueUniqueWork(TAG, ExistingWorkPolicy.REPLACE, dailyWorkRequest);
        scheduled = true;
    }

    public static long getFollowUpCount() {
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        int count = 0;
        String visitType = "General";
        List<FollowUpModel> modelList = new ArrayList<FollowUpModel>();
        String table = "tbl_patient";
        Date cDate = new Date();
        FollowUpModel model = new FollowUpModel();
        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(cDate);

        db.beginTransaction();
        String query = "SELECT * from (SELECT a.uuid, a.sync, a.patientuuid, a.startdate, a.enddate, b.uuid, b.first_name, b.middle_name, b.last_name, b.date_of_birth, b.openmrs_id, c.value AS speciality, o.value FROM tbl_visit a, tbl_patient b, tbl_encounter d, tbl_obs o, tbl_visit_attribute c WHERE a.uuid = c.visit_uuid AND  a.enddate is NOT NULL AND a.patientuuid = b.uuid AND a.uuid = d.visituuid AND d.uuid = o.encounteruuid AND o.conceptuuid in ('e8caffd6-5d22-41c4-8d6a-bc31a44d0c86', (Select conceptuuid from tbl_obs Where d.uuid = encounteruuid AND value like '%Do you want us to follow-up?%')) ORDER BY startdate DESC) as sub GROUP BY patientuuid ORDER BY startdate DESC";
        final Cursor searchCursor = db.rawQuery(query, null);
        if (searchCursor.moveToFirst()) {
            do {
                try {
                    String visitStartDateFollowup = searchCursor.getString(searchCursor.getColumnIndexOrThrow("startdate"));
                    String visitFollowup = "";
                    if (searchCursor.getString(searchCursor.getColumnIndexOrThrow("value")).contains(" Do you want us to follow-up? - Yes")) {
                        visitType = "Diabetes Follow-up";
                        visitFollowup = searchCursor.getString(searchCursor.getColumnIndexOrThrow("value")).substring(68, 79);
                        visitFollowup = visitFollowup.replaceAll("/", "-");
                        Date requiredFormat = new SimpleDateFormat("dd-MMM-yyyy").parse(visitFollowup);
                        SimpleDateFormat outputDateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
                        visitFollowup = outputDateFormat.format(requiredFormat);
                    } else {
                        visitFollowup = searchCursor.getString(searchCursor.getColumnIndexOrThrow("value")).substring(0, 10);
                    }


                    SimpleDateFormat sd1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
                    Date startDate = sd1.parse(visitStartDateFollowup);
                    Date followUp = new SimpleDateFormat("dd-MM-yyyy").parse(visitFollowup);
                    String newStartDate = new SimpleDateFormat("dd-MM-yyyy").format(startDate);
                    Date currentD = new SimpleDateFormat("dd-MM-yyyy").parse(currentDate);
                    int value = followUp.compareTo(currentD);

                    if (visitType.equalsIgnoreCase("Diabetes Follow-up")) {
                        if (value == -1 || value == 0) {
                            count++;
                        }
                    } else {
                        String mSeverityValue = getSeverity(searchCursor.getString(searchCursor.getColumnIndexOrThrow("patientuuid")), db);
                        int days = mGetDaysAccording(newStartDate);
                        String mValue = "";
                        if (!mSeverityValue.contains("Do you want us to follow-up?")) {
                            String[] arrSplit_2 = mSeverityValue.split("-");
                            mValue = arrSplit_2[arrSplit_2.length - 1];
                        }
                        if (value == -1) {

                            if (days > 0 && days < 11) {
                                Log.d("mSeverityValue", "mSeverityValue++ " + mSeverityValue);
                                Log.d("days", "days++ " + days);

                                if (days % 2 == 0) {
                                    if (mValue.trim().equalsIgnoreCase("Mild.") || mValue.trim().equalsIgnoreCase("Moderate.") || mValue.trim().contains("Moderate.") || mValue.trim().contains("Mild.")) {
                                        count++;
                                    } else if (mValue.trim().contains("Severe.") || mValue.trim().equalsIgnoreCase("Severe.")) {
                                        count++;
                                    } else {
                                        count++;
                                    }
                                } else {
                                    if (mValue.trim().contains("Severe.") || mValue.trim().equalsIgnoreCase("Severe.")) {
                                        count++;
                                    } else {
                                        count++;
                                    }
                                }

                            } else {
                                count++;
                            }
                        } else if (value > 0) {

                            if (days > 0 && days < 11 && days != 0) {
                                Log.d("mSeverityValue", "mSeverityValue++ " + mSeverityValue);
                                Log.d("days", "days++ " + days);

                                if (days % 2 == 0) {
                                    if (mValue.trim().equalsIgnoreCase("Mild.") || mValue.trim().equalsIgnoreCase("Moderate.") || mValue.trim().contains("Moderate.") || mValue.trim().contains("Mild.")) {
                                        count++;
                                    } else if (mValue.trim().contains("Severe.") || mValue.trim().equalsIgnoreCase("Severe.")) {
                                        count++;
                                    } else {
// todo No need to added
                                    }
                                } else {
                                    if (mValue.trim().contains("Severe.") || mValue.trim().equalsIgnoreCase("Severe.")) {
                                        count++;
                                    }

                                }

                            }
                        } else {
                            count++;
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            while (searchCursor.moveToNext());
        }
        searchCursor.close();
        db.setTransactionSuccessful();
        db.endTransaction();
        return count;
    }

//    public static long getFollowUpCount(SQLiteDatabase db) {
//        /*final Cursor searchCursor = db.rawQuery("SELECT * FROM tbl_patient as p where p.uuid in (select v.patientuuid from tbl_visit as v  where  v.uuid in (select e.visituuid from tbl_encounter as e where e.uuid in (select o.encounteruuid from tbl_obs as o where o.value like '%Moderate%' or o.value like '%Mild%' or 'Severe')))", null);
//        boolean result = searchCursor.moveToFirst();
//        searchCursor.close();
//        return result;*/
////        return DatabaseUtils.longForQuery(db, "SELECT COUNT(*) as count FROM tbl_patient as p where p.uuid in (select v.patientuuid from tbl_visit as v where v.enddate is NULL and v.uuid in (select e.visituuid from tbl_encounter as e where e.uuid in (select o.encounteruuid from tbl_obs as o where o.conceptuuid = ? and (o.value like '%Moderate%' or o.value like '%Mild%' or o.value like '%Severe%'))))", new String[]{UuidDictionary.PHYSICAL_EXAMINATION});
//        int count = 0;
//        Date cDate = new Date();
//        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(cDate);
//        String query = "SELECT * FROM tbl_patient as p where p.uuid in (select v.patientuuid from tbl_visit as v where v.enddate like '%Sep 12, 2021%' or v.uuid in (select e.visituuid from tbl_encounter as e where e.uuid in (select o.encounteruuid from tbl_obs as o where o.conceptuuid = ? and o.value like '%"+ currentDate +"%')))";
//        final Cursor cursor = db.rawQuery(query,  new String[]{UuidDictionary.FOLLOW_UP_VISIT});
//        try {
//            if (cursor.moveToFirst()) {
//                do {
//                    String uuid = cursor.getString(cursor.getColumnIndexOrThrow("uuid"));
//                    String severity = getSeverity(uuid, db);
//                    if (severity == null)
//                        continue;
//                    count++;
//                } while (cursor.moveToNext());
//                cursor.close();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return count;
//    }

//    public static long getFollowUpCount(SQLiteDatabase db) {
//
//        int count = 0;
//        Date cDate = new Date();
//        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(cDate);
//        String query = "SELECT a.uuid, a.sync, a.patientuuid, a.startdate, a.enddate, b.uuid, b.first_name, b.middle_name, b.last_name, b.date_of_birth, b.openmrs_id, c.value AS speciality, o.value FROM tbl_visit a, tbl_patient b, tbl_encounter d, tbl_obs o, tbl_visit_attribute c WHERE a.uuid = c.visit_uuid AND  a.enddate is NOT NULL AND a.patientuuid = b.uuid AND a.uuid = d.visituuid AND d.uuid = o.encounteruuid AND o.conceptuuid = ? AND o.value is NOT NULL GROUP BY a.patientuuid";
//        final Cursor cursor = db.rawQuery(query, new String[]{UuidDictionary.FOLLOW_UP_VISIT});
//        if (cursor != null) {
//            if (cursor.moveToFirst()) {
//                do {
//                    try {
//                        String visitDateFollowup = cursor.getString(cursor.getColumnIndexOrThrow("startdate"));
//                        String followUpDate = cursor.getString(cursor.getColumnIndexOrThrow("value")).substring(0,10);
//                        SimpleDateFormat sd1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
//                        Date startDate = sd1.parse(visitDateFollowup);
//
//                        String newStartDate = new SimpleDateFormat("dd-MM-yyyy").format(startDate);
//                        Date currentD = new SimpleDateFormat("dd-MM-yyyy").parse(currentDate);
//
//                        Date followUp = new SimpleDateFormat("dd-MM-yyyy").parse(followUpDate);
////                        Date currentD = new SimpleDateFormat("dd-MM-yyyy").parse(currentDate);
//                        int value = followUp.compareTo(currentD);
//                        String mSeverityValue = getSeverity(cursor.getString(cursor.getColumnIndexOrThrow("uuid")),db);
//                        Log.d("mSeverityValue", "mSeverityValue++ " + mSeverityValue);
//                        String[] arrSplit_2 = mSeverityValue.split("-");
//                        String mValue = arrSplit_2[arrSplit_2.length - 1];
//                        int days = mGetDaysAccording(newStartDate);
//
//                        if (value == -1) {
////                            count++;
//
//                            if (days > 0 && days < 11) {
//                                if (days % 2 == 0) {
//                                    if (mValue.trim().equalsIgnoreCase("Mild.") || mValue.trim().equalsIgnoreCase("Moderate.") || mValue.trim().contains("Moderate.")|| mValue.trim().contains("Mild."))
//                                    {
//                                        count++;
//                                    } else if (mValue.contains("Severe.")||mValue.trim().equalsIgnoreCase("Severe.")) {
//                                        count++;
//                                    } else {
//// todo No need to added
//                                    }
//                                }
//                                else {
//                                    if (mValue.contains("Severe.")||mValue.trim().equalsIgnoreCase("Severe.")) {
//                                        count++;
//                                    } else {
//
//                                    }
//                                }
//
//                            } else { // todo No need to added
//                            }
//                        }
//                        else if(value>0) {
//                            if (days > 0 && days < 11 && days != 0) {
//                                if (days % 2 == 0) {
//                                    if (mValue.trim().equalsIgnoreCase("Mild.") || mValue.trim().equalsIgnoreCase("Moderate.") || mValue.trim().contains("Moderate.") || mValue.trim().contains("Mild.")) {
//                                        count++;
//                                    } else if (mValue.trim().equalsIgnoreCase("Severe.")|| mValue.trim().contains("Severe.")) {
//                                        count++;
//                                    } else {
//// todo No need to added
//                                    }
//                                } else {
//                                    if (mValue.trim().contains("Severe.") ||mValue.trim().equalsIgnoreCase("Severe.")) {
//                                        count++;
//                                    } else {
//
//                                    }
//                                }
//
//                            }
//
//                        }
//                        else{
//                            count++;
//                        }
//
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//
//                } while (cursor.moveToNext());
//            }
//            if (cursor != null) {
//                cursor.close();
//            }
//        }
//        return count;
//    }

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

    private String msetSeverity(String patientUid) {
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
}
