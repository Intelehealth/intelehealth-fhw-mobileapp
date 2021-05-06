package org.intelehealth.app.services.firebase_services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.shivam.androidwebrtc.CompleteActivity;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.homeActivity.HomeActivity;
import org.intelehealth.app.utilities.OfflineLogin;

/**
 * Created by Dexter Barretto on 5/25/17.
 * Github : @dbarretto
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = MyFirebaseMessagingService.class.getSimpleName();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        //Displaying data in log
        //It is optional
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        Log.d(TAG, "Notification Message Title: " + remoteMessage.getNotification().getTitle());
        Log.d(TAG, "Notification Message Body: " + remoteMessage.getNotification().getBody());
        Log.d(TAG, "Notification Message Body: " + remoteMessage.getData());
        //  {nurseId=28cea4ab-3188-434a-82f0-055133090a38, doctorName=doctor1, roomId=b60263f2-5716-4047-aaf5-7c13199b7f0c}
        if ("Incoming call".equals(remoteMessage.getNotification().getTitle())) {
            Intent in = new Intent(this, CompleteActivity.class);
            String roomId = remoteMessage.getData().get("roomId");
            String doctorName = remoteMessage.getData().get("doctorName");
            String nurseId = remoteMessage.getData().get("nurseId");
            in.putExtra("roomId", roomId);
            in.putExtra("isInComingRequest", true);
            in.putExtra("doctorname", doctorName);
            in.putExtra("nurseId", nurseId);
            startActivity(in);
        } else {
            parseMessage(remoteMessage.getNotification().getTitle(),
                    remoteMessage.getNotification().getBody());
        }
    }

    private void parseMessage(String messageTitle, String messageBody) {

        switch (messageBody) {
            case "INVALIDATE_OFFLINE_LOGIN": {
                //Invalidating Offline credentials
                OfflineLogin.getOfflineLogin().invalidateLoginCredentials();
                break;
            }
            case "UPDATE_MIND_MAPS": {
            }
            default:
                //Calling method to generate notification
                sendNotification(messageBody);
        }

    }

    //This method is only generating push notification
    //It is same as we did in earlier posts
    private void sendNotification(String messageBody) {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);
        String channelId = "CHANNEL_ID";

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Firebase Push Notification")
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel
                    (channelId, "Default Channel", NotificationManager.IMPORTANCE_HIGH);

            notificationManager.createNotificationChannel(channel);
        }


        notificationManager.notify(0, notificationBuilder.build());
    }
}
