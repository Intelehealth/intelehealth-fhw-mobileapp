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
    private static final String CHANNEL_ID = "EKAL221";
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
//        Intent intent = new Intent(CallListenerBackgroundService.this, RestartServiceReceiver.class);
//        intent.setAction("org.intelehealth.app.RTC_SERVICE_START");
//        sendBroadcast(intent);
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
                .setOngoing(true)
                //.setTicker(getText(R.string.ticker_text))
                .build();

        // Notification ID cannot be 0.
        startForeground(ONGOING_NOTIFICATION_ID, notification);
        //do heavy work on a background thread
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