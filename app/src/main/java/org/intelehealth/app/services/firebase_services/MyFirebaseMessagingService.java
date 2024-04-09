package org.intelehealth.app.services.firebase_services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.homeActivity.HomeActivity;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.utilities.OfflineLogin;
import org.intelehealth.app.webrtc.activity.NASChatActivity;
import org.intelehealth.klivekit.call.ui.activity.VideoCallActivity;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Dexter Barretto on 5/25/17.
 * Github : @dbarretto
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = MyFirebaseMessagingService.class.getSimpleName();
    private static final String ACTION_NAME = "org.intelehealth.app.RTC_MESSAGING_EVENT";

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        //Displaying data in log
        //It is optional
        //Log.d(TAG, "From: " + remoteMessage.getFrom());
        //Log.d(TAG, "Notification Message Title: " + remoteMessage.getNotification().getTitle());
        //Log.d(TAG, "Notification Message Body: " + remoteMessage.getNotification().getBody());
        Log.d(TAG, "Notification Message Data: " + remoteMessage.getData());
        //  {nurseId=28cea4ab-3188-434a-82f0-055133090a38, doctorName=doctor1, roomId=b60263f2-5716-4047-aaf5-7c13199b7f0c}

        if (remoteMessage.getData().containsKey("actionType")) {
            if (remoteMessage.getData().get("actionType").equals("VIDEO_CALL")) {
                Log.d(TAG, "actionType : VIDEO_CALL");
                Intent in = new Intent(this, VideoCallActivity.class);
                String roomId = remoteMessage.getData().get("roomId");
                String doctorName = remoteMessage.getData().get("doctorName");
                String nurseId = remoteMessage.getData().get("nurseId");
                in.putExtra("roomId", roomId);
                in.putExtra("isInComingRequest", true);
                in.putExtra("doctorname", doctorName);
                in.putExtra("nurseId", nurseId);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
                int callState = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE)).getCallState();
                if (callState == TelephonyManager.CALL_STATE_IDLE) {
                    startActivity(in);
                } else {
                    sendNotification(remoteMessage, null);
                }

            } else if (remoteMessage.getData().get("actionType").equals("TEXT_CHAT")) {
                try {
                    Log.d(TAG, "actionType : TEXT_CHAT");
                    String fromUUId = remoteMessage.getData().get("toUser");
                    String toUUId = remoteMessage.getData().get("fromUser");
                    String patientUUid = remoteMessage.getData().get("patientId");
                    String visitUUID = remoteMessage.getData().get("visitId");
                    String patientName = remoteMessage.getData().get("patientName");
                    JSONObject connectionInfoObject = new JSONObject();
                    connectionInfoObject.put("fromUUID", fromUUId);
                    connectionInfoObject.put("toUUID", toUUId);
                    connectionInfoObject.put("patientUUID", patientUUid);


                    Intent chatIntent = new Intent(this, NASChatActivity.class);
                    chatIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    chatIntent.putExtra("patientName", patientName);
                    chatIntent.putExtra("visitUuid", visitUUID);
                    chatIntent.putExtra("patientUuid", patientUUid);
                    chatIntent.putExtra("fromUuid", fromUUId);
                    chatIntent.putExtra("toUuid", toUUId);
                    PendingIntent pendingIntent = null;  // after s+ version it is needed to set IMMUTABLE.

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        pendingIntent = PendingIntent.getActivity(this, 0, chatIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                    } else {
                        pendingIntent = PendingIntent.getActivity(this, 0, chatIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT);
                    }

                    sendNotification(remoteMessage, pendingIntent);

                    PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                    String packageName = pInfo.packageName;

                    Intent intent = new Intent(ACTION_NAME);
                    intent.putExtra("visit_uuid", visitUUID);
                    intent.putExtra("connection_info", connectionInfoObject.toString());
                    intent.setComponent(new ComponentName(packageName,
                            "org.intelehealth.app.services.firebase_services.RTCMessageReceiver"));
                    intent.setPackage(IntelehealthApplication.getInstance().getPackageName());
                    getApplicationContext().sendBroadcast(intent);


                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }

        } else {
            parseMessage(remoteMessage);
        }
    }


    private void parseMessage(RemoteMessage remoteMessage) {
        String messageTitle = remoteMessage.getNotification().getTitle();
        String messageBody = remoteMessage.getNotification().getBody();
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
                sendNotification(remoteMessage, null);
        }

    }


    //This method is only generating push notification
    //It is same as we did in earlier posts
    private void sendNotification(RemoteMessage remoteMessage, PendingIntent pendingIntent) {
        String messageTitle = remoteMessage.getNotification().getTitle();
        String messageBody = remoteMessage.getNotification().getBody();

        if (pendingIntent == null) {
            Intent notificationIntent = new Intent(this, HomeActivity.class);
            notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            } else {
                pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            }
        }
        String channelId = "CHANNEL_ID";

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                //.setContentTitle("Firebase Push Notification")
                .setContentTitle(messageTitle)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setColor(getResources().getColor(R.color.white))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        /*NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);*/

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

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


        notificationManager.notify(1, notificationBuilder.build());

    }
}
