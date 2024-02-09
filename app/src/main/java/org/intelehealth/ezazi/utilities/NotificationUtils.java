package org.intelehealth.ezazi.utilities;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.provider.Settings;

import androidx.core.app.NotificationCompat;

import org.intelehealth.ezazi.R;
import org.intelehealth.ezazi.activities.visitSummaryActivity.TimelineVisitSummaryActivity;


public class NotificationUtils {


    private String channelId = "1";
    private String channelName = "intelehealth";
    private int mId = 1;
    Context context;
    private static final String NOTIFICATION_CHANNEL_ID_5MinsBefore = "Channel 5 min";
    private static final String NOTIFICATION_CHANNEL_ID_15MinsBefore = "Channel 15 min";

//    notifcation id for the inteleHealth org
//    #1 for sync module
//    #2 for patient Details upload and download
//    #3 for visit uploadand download and end visit
//    #4 for images upload and download either obs and patient profile
//

    public void showNotifications(String title, String text, int notificationId, Context context) {
        this.context = context;
        NotificationManager mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            String channelid = String.valueOf(notificationId);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(channelid, channelName, importance);
            mNotifyManager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, channelId);
        mBuilder.setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_cloud_upload);
        mBuilder.setProgress(100, 0, true);
        mNotifyManager.notify(notificationId, mBuilder.build());

    }

    public void showNotificationProgress(String title, String text, int notifcationId, Context context, Integer progress) {
        this.context = context;
        NotificationManager mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            String channelid = String.valueOf(notifcationId);
            NotificationChannel mChannel = new NotificationChannel(channelid, channelName, importance);
            mNotifyManager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, channelId);
        mBuilder.setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_cloud_upload);
        mBuilder.setProgress(100, progress, false);
        mNotifyManager.notify(notifcationId, mBuilder.build());

    }

    public void DownloadDone(String title, String text, int notificationId, Context context) {
        this.context = context;
        NotificationManager mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        mNotifyManager.cancel(mId);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            String channelid = String.valueOf(notificationId);
            NotificationChannel mChannel = new NotificationChannel(channelid, channelName, importance);
            mNotifyManager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, channelId);
        mBuilder.setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_cloud_upload);
        mNotifyManager.notify(notificationId, mBuilder.build());

    }

    public void clearAllNotifications(Context context) {

        this.context = context;
        NotificationManager mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        mNotifyManager.cancelAll();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(channelId, channelName, importance);
            mNotifyManager.createNotificationChannel(mChannel);
        }

        mNotifyManager.cancelAll();
    }

    public void showNotifications_noProgress(String title, String text, Context context) {
        this.context = context;
        NotificationManager mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        //mahiti added
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(channelId, channelName, importance);
            mNotifyManager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, channelId);
//        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, new Intent(context, HomeActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

//        mBuilder.setContentIntent(contentIntent);
      /*  mBuilder.setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_cloud_upload);
        //mBuilder.setProgress(100, 0, true);
*/

        Notification notification = mBuilder
                .setSmallIcon(R.drawable.ic_cloud_upload)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setContentText(text).build();
        mNotifyManager.notify(mId, mBuilder.build());
    }


    public void createTimelineNotification(Context mcontext, Intent intentPassed) {

        String patientName = intentPassed.getStringExtra("patientNameTimeline");
        int time = intentPassed.getIntExtra("timeTag", 0);
        String patientUuid = intentPassed.getStringExtra("patientUuid");
        String visitUuid = intentPassed.getStringExtra("visitUuid");

        Intent intent = new Intent(mcontext, TimelineVisitSummaryActivity.class);
        intent.putExtra("patientNameTimeline", patientName);
        intent.putExtra("patientUuid", patientUuid);
        intent.putExtra("visitUuid", visitUuid);
        intent.putExtra("fromNotificationTag", "Notification");

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        if (time == 5)
            notification5MinsBefore(mcontext, patientName, intent);
        else if (time == 15)
            notification15MinsBefore(mcontext, patientName, intent);
    }

    private void notification5MinsBefore(Context mcontext, String patientName, Intent intent) {
        PendingIntent resultPendingIntent = PendingIntent.getActivity(mcontext,
                5, intent,
                getPendingIntentFlag());

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mcontext, NOTIFICATION_CHANNEL_ID_5MinsBefore);
        mBuilder.setSmallIcon(R.mipmap.ic_launcher);
        mBuilder.setContentTitle(mcontext.getString(R.string.notificationTitle_Timeline, patientName))
                .setContentText(mcontext.getString(R.string.notificationTimeline_Description))
                .setAutoCancel(true) // user clicks than notifi is cleared from status bar
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(resultPendingIntent);

        NotificationManager mNotificationManager = (NotificationManager)
                mcontext.getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID_5MinsBefore,
                    "NOTIFICATION_CHANNEL_NAME_5min", importance);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            assert mNotificationManager != null;
            mBuilder.setChannelId(NOTIFICATION_CHANNEL_ID_5MinsBefore);
            mNotificationManager.createNotificationChannel(notificationChannel);
        }
        assert mNotificationManager != null;
        mNotificationManager.notify(5, mBuilder.build());


    }

    private void notification15MinsBefore(Context mcontext, String patientName, Intent intent) {
        PendingIntent resultPendingIntent = PendingIntent.getActivity(mcontext,
                15, intent,
                getPendingIntentFlag());

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mcontext, NOTIFICATION_CHANNEL_ID_15MinsBefore);
        mBuilder.setSmallIcon(R.mipmap.ic_launcher);
        mBuilder.setContentTitle(mcontext.getString(R.string.notificationTitle_Timeline, patientName))
                .setContentText("Is coming for data capture in 15mins")
                .setAutoCancel(true) // user clicks than notifi is cleared from status bar
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(resultPendingIntent);

        NotificationManager mNotificationManager = (NotificationManager)
                mcontext.getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID_15MinsBefore,
                    "NOTIFICATION_CHANNEL_NAME_15min", importance);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            assert mNotificationManager != null;
            mBuilder.setChannelId(NOTIFICATION_CHANNEL_ID_15MinsBefore);
            mNotificationManager.createNotificationChannel(notificationChannel);
        }
        assert mNotificationManager != null;
        mNotificationManager.notify(15, mBuilder.build());

    }

   /* public static int getPendingIntentFlag() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                ? PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT : PendingIntent.FLAG_UPDATE_CURRENT;
    }*/

    public static int getPendingIntentFlag() {
        int flag;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            flag = PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT;
        } else {
            flag = PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT;
        }
        return flag;
    }
}
