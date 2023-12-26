package org.intelehealth.ekalarogya.webrtc.notification;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import org.intelehealth.ekalarogya.R;


/**
 * Created by Vaghela Mithun R. on 02-08-2023 - 16:21.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public class AppNotification {
    private AppNotification() {

    }

    private PendingIntent pendingIntent;
    private String title;
    private String message;

    private int notificationId = 100100;

    public void sendNotification(Context context) {
        String channelId = "CHANNEL_ID";

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                //.setContentTitle("Firebase Push Notification")
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Default Channel";
            String description = "Default Channel description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelId, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            notificationManager.createNotificationChannel(channel);
        }

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManager.notify(notificationId, notificationBuilder.build());
    }


    public static class Builder {
        private Context context;
        private AppNotification appNotification;

        public Builder(Context context) {
            this.context = context;
            appNotification = new AppNotification();
        }

        public Builder title(String title) {
            appNotification.title = title;
            return this;
        }

        public Builder body(String body) {
            appNotification.message = body;
            return this;
        }

        public Builder pendingIntent(PendingIntent pendingIntent) {
            appNotification.pendingIntent = pendingIntent;
            return this;
        }

        public void send() {
            appNotification.sendNotification(context);
        }
    }
}
