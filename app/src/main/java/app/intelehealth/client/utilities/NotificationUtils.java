package app.intelehealth.client.utilities;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import androidx.core.app.NotificationCompat;

import app.intelehealth.client.R;


public class NotificationUtils {


    private String channelId = "1";
    private String channelName = "intelehealth";
    private int mId = 1;
    Context context;

//    notifcation id for the inteleHealth app
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
                .setAutoCancel(true).setContentTitle(title)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setContentText(text).build();
        mNotifyManager.notify(mId, mBuilder.build());

    }
}
