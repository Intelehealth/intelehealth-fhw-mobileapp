package org.intelehealth.ezazi.services.firebase_services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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
import com.google.gson.Gson;

import org.intelehealth.ezazi.R;
import org.intelehealth.ezazi.activities.homeActivity.HomeActivity;
import org.intelehealth.ezazi.activities.visitSummaryActivity.ShiftChangeData;
import org.intelehealth.ezazi.activities.visitSummaryActivity.TimelineVisitSummaryActivity;
import org.intelehealth.ezazi.app.IntelehealthApplication;
import org.intelehealth.ezazi.database.dao.ProviderDAO;
import org.intelehealth.ezazi.database.dao.SyncDAO;
import org.intelehealth.ezazi.ui.rtc.activity.EzaziChatActivity;
import org.intelehealth.ezazi.ui.rtc.activity.EzaziVideoCallActivity;
import org.intelehealth.ezazi.utilities.AppNotification;
import org.intelehealth.ezazi.utilities.NotificationUtils;
import org.intelehealth.ezazi.utilities.OfflineLogin;
import org.intelehealth.ezazi.utilities.SessionManager;
import org.intelehealth.ezazi.utilities.exception.DAOException;
import org.intelehealth.klivekit.model.ChatMessage;
import org.intelehealth.klivekit.model.RtcArgs;
import org.intelehealth.klivekit.utils.FirebaseUtils;
import org.intelehealth.klivekit.utils.Manager;
import org.json.JSONObject;

import java.util.Map;

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
        saveToken();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "onMessageReceived:remote " + new Gson().toJson(remoteMessage));
        Log.d(TAG, "onMessageReceived:notification " + new Gson().toJson(remoteMessage.getNotification()));

        //Displaying data in log
        //It is optional
        //Log.d(TAG, "From: " + remoteMessage.getFrom());
        //Log.d(TAG, "Notification Message Title: " + remoteMessage.getNotification().getTitle());
        //Log.d(TAG, "Notification Message Body: " + remoteMessage.getNotification().getBody());
        Log.d(TAG, "Notification Message Data: " + remoteMessage.getData());
        //  {nurseId=28cea4ab-3188-434a-82f0-055133090a38, doctorName=doctor1, roomId=b60263f2-5716-4047-aaf5-7c13199b7f0c}

        if (new SessionManager(this).isLogout()) return;


        if (remoteMessage.getData().containsKey("actionType")) {
            if (remoteMessage.getData().get("actionType").equals("VIDEO_CALL")) {
                Log.d(TAG, "actionType : VIDEO_CALL");
                Intent in = new Intent(this, EzaziVideoCallActivity.class);
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
                    Gson gson = new Gson();
                    ChatMessage chatMessage = gson.fromJson(gson.toJson(remoteMessage.getData()), ChatMessage.class);
                    RtcArgs args = new RtcArgs();
                    args.setPatientName(chatMessage.getPatientName());
                    args.setPatientId(chatMessage.getPatientId());
                    args.setVisitId(chatMessage.getVisitId());
                    args.setNurseId(chatMessage.getToUser());
                    args.setDoctorUuid(chatMessage.getFromUser());

                    try {
                        String title = new ProviderDAO().getProviderName(args.getDoctorUuid());
                        new AppNotification.Builder(this)
                                .title(title)
                                .body(chatMessage.getMessage())
                                .pendingIntent(EzaziChatActivity.getPendingIntent(this, args))
                                .send();
                    } catch (DAOException e) {
                        throw new RuntimeException(e);
                    }


//                    String fromUUId = remoteMessage.getData().get("toUser");
//                    String toUUId = remoteMessage.getData().get("fromUser");
//                    String patientUUid = remoteMessage.getData().get("patientId");
//                    String visitUUID = remoteMessage.getData().get("visitId");
//                    String patientName = remoteMessage.getData().get("patientName");
//                    JSONObject connectionInfoObject = new JSONObject();
//                    connectionInfoObject.put("fromUUID", fromUUId);
//                    connectionInfoObject.put("toUUID", toUUId);
//                    connectionInfoObject.put("patientUUID", patientUUid);
//
//
//                    Intent chatIntent = new Intent(this, EzaziChatActivity.class);
//                    chatIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                    chatIntent.putExtra("patientName", patientName);
//                    chatIntent.putExtra("visitUuid", visitUUID);
//                    chatIntent.putExtra("patientUuid", patientUUid);
//                    chatIntent.putExtra("fromUuid", fromUUId);
//                    chatIntent.putExtra("toUuid", toUUId);
//                    PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, chatIntent,
//                            NotificationUtils.getPendingIntentFlag());
//                    sendNotification(remoteMessage, pendingIntent);
//
//
//                    Intent intent = new Intent(ACTION_NAME);
//                    intent.putExtra("visit_uuid", visitUUID);
//                    intent.putExtra("connection_info", connectionInfoObject.toString());
//                    intent.setComponent(new ComponentName("org.intelehealth.ezazi", "org.intelehealth.ezazi.utilities.RTCMessageReceiver"));
//                    getApplicationContext().sendBroadcast(intent);


                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (remoteMessage.getData().get("actionType").equals("SHIFT_CHANGE")) {

                // title : New patient for you.
                // content : "patient_name" patient has been assigned to you by "HW1 name"

                try {

                    Gson gson = new Gson();
                    ShiftChangeData shiftChangeData = gson.fromJson(gson.toJson(remoteMessage.getData()), ShiftChangeData.class);
                    RtcArgs args = new RtcArgs();
                    args.setProviderID(shiftChangeData.getProviderID());
                    args.setTag(shiftChangeData.getTag());
                    args.setAssignorNurseName(shiftChangeData.getAssignorNurse());
                    new SyncDAO().pullData_Background(this);

                    //sendNotification(remoteMessage, TimelineVisitSummaryActivity.getPendingIntent(this, args));
//                    String content = (shiftChangeData.getPatientNameTimeline()) + " patient has been assigned to you by " + shiftChangeData.getAssignorNurse();
                    new AppNotification.Builder(this)
                            .title(shiftChangeData.getTitle())
                            .body(shiftChangeData.getBody())
                            .pendingIntent(TimelineVisitSummaryActivity.getPendingIntent(this, args))
                            .send();
                } catch (Exception e) {
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
            Intent intent = new Intent(this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            pendingIntent = PendingIntent.getActivity(this, 0, intent,
                    NotificationUtils.getPendingIntentFlag());
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

    private void saveToken() {
        ProviderDAO providerDAO = new ProviderDAO();
        SessionManager sessionManager = new SessionManager(this);
        Manager.getInstance().setBaseUrl("https://" + sessionManager.getServerUrl());
        // save fcm reg. token for chat (Video)
        try {
            FirebaseUtils.saveToken(this, providerDAO.getUserUuid(sessionManager.getProviderID()), IntelehealthApplication.getInstance().refreshedFCMTokenID, sessionManager.getAppLanguage());
        } catch (DAOException e) {
            throw new RuntimeException(e);
        }
    }
}
