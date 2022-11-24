package org.intelehealth.ekalarogya.services.firebase_services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.intelehealth.ekalarogya.R;

import org.intelehealth.ekalarogya.activities.homeActivity.HomeActivity;
import org.intelehealth.ekalarogya.app.AppConstants;
import org.intelehealth.ekalarogya.app.IntelehealthApplication;
import org.intelehealth.ekalarogya.utilities.SessionManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

public class CallListenerBackgroundService extends Service {
    private static final String CHANNEL_ID = "N221";
    private static final int ONGOING_NOTIFICATION_ID = 1001;
    private static final String TAG = CallListenerBackgroundService.class.getName();
    public String refreshedFCMTokenID = "";

    private static CallListenerBackgroundService instance = null;

    public static boolean isInstanceCreated() {
        return instance != null;
    }

    public static CallListenerBackgroundService getInstance() {
        return instance;
    }

    public void stopForegroundService() {
        stopForeground(true);
        stopSelf();
    }

    public CallListenerBackgroundService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(TAG, "onCreate");
        instance = this;
        refreshedFCMTokenID = IntelehealthApplication.getInstance().refreshedFCMTokenID;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "onDestroy");
        instance = null;
        stopForeground(true);
        Intent intent = new Intent(CallListenerBackgroundService.this, RestartServiceReceiver.class);
        intent.setAction("org.intelehealth.app.RTC_SERVICE_START");
        sendBroadcast(intent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(TAG, "onStartCommand");
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, HomeActivity.class);

        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        } else {
            pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getText(R.string.notification_title))
                //.setContentText(getText(R.string.notification_message))
                .setSmallIcon(R.drawable.ic_stat_bg_service)
                .setContentIntent(pendingIntent)
                .setShowWhen(false)
                .setNotificationSilent()
                //.setTicker(getText(R.string.ticker_text))
                .build();

        // Notification ID cannot be 0.
        startForeground(ONGOING_NOTIFICATION_ID, notification);
        //do heavy work on a background thread

        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance(AppConstants.getFirebaseRTDBUrl());
        DatabaseReference myRef = database.getReference(AppConstants.getFirebaseRTDBRootRef() + new SessionManager(this).getProviderID() + "/VIDEO_CALL");
        if (myRef != null)
            //myRef.setValue("Hello, World!");
            // Read from the database
            myRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    HashMap value = (HashMap) dataSnapshot.getValue();
                    //{doctorName=Demo doctor1, nurseId=8d61869b-14d7-4c16-9c7a-a6f1aaaa3c0d, roomId=df412e7e-9020-49ed-9712-1937ad46af9b, timestamp=1628564570611}
                    Log.d(TAG, "Value is: " + value);
                    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    // Vibrate for 500 milliseconds
               /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    //deprecated in API 26
                    v.vibrate(500);
                }*/
                    if (value == null) return;
                    String device_token = String.valueOf(value.get("device_token"));
                    if (!device_token.equals(refreshedFCMTokenID)) return;
                    Bundle bundle = new Bundle();
                    bundle.putString("doctorName", String.valueOf(value.get("doctorName")));
                    bundle.putString("nurseId", String.valueOf(value.get("nurseId")));
                    bundle.putString("roomId", String.valueOf(value.get("roomId")));
                    bundle.putString("timestamp", String.valueOf(value.get("timestamp")));
                    bundle.putString("actionType", "VIDEO_CALL");

                    boolean isCallEnded = Boolean.parseBoolean(String.valueOf(value.get("callEnded")));
                    bundle.putBoolean("callEnded", isCallEnded);

                    if (isCallEnded) {
                        Intent intent = new Intent(CallListenerBackgroundService.this, CallRTCNotifyReceiver.class);
                        intent.putExtras(bundle);
                        intent.setAction("org.intelehealth.app.RTC_MESSAGE_EVENT");
                        sendBroadcast(intent);
                    }

                    boolean isOldNotification = true;
                    if (value.containsKey("timestamp")) {
                        String timestamp = String.valueOf(value.get("timestamp"));


                        Date date = new Date();
                        if (timestamp != null) {
                            date.setTime(Long.parseLong(timestamp));
                            SimpleDateFormat dateFormatter = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss"); //this format changeable
                            dateFormatter.setTimeZone(TimeZone.getDefault());

                            try {
                                Date ourDate = dateFormatter.parse(dateFormatter.format(date));
                                long seconds = 0;
                                if (ourDate != null) {
                                    seconds = Math.abs(new Date().getTime() - ourDate.getTime()) / 1000;
                                }
                                //stopForeground(true);
                                //stopSelf();
                                Log.v(TAG, "Notification time - " + ourDate);
                                Log.v(TAG, "seconds - " + seconds);
                                if (seconds <= 10) {
                                    isOldNotification = false;
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    if (!isOldNotification) {
                        Intent intent = new Intent(CallListenerBackgroundService.this, CallRTCNotifyReceiver.class);
                        intent.putExtras(bundle);
                        intent.setAction("org.intelehealth.app.RTC_MESSAGE_EVENT");
                        sendBroadcast(intent);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Failed to read value
                    Log.w(TAG, "Failed to read value.", error.toException());
                }
            });


        return START_NOT_STICKY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
}