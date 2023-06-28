package org.intelehealth.ezazi.firebase;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;

import androidx.annotation.NonNull;

import com.github.ajalt.timberkt.Timber;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import org.intelehealth.ezazi.app.AppConstants;
import org.intelehealth.ezazi.services.firebase_services.CallListenerBackgroundService;
import org.intelehealth.ezazi.services.firebase_services.CallRTCNotifyReceiver;
import org.intelehealth.ezazi.services.firebase_services.FirebaseRealTimeDBUtils;
import org.intelehealth.ezazi.utilities.SessionManager;
import org.intelehealth.klivekit.model.RtcArgs;
import org.intelehealth.klivekit.utils.Constants;
import org.intelehealth.klivekit.utils.RtcUtilsKt;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

/**
 * Created by Vaghela Mithun R. on 28-06-2023 - 18:07.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public class RealTimeDataUpdateListener {
    private final Context context;

    public RealTimeDataUpdateListener(Context context) {
        this.context = context;
    }

    private static final String TAG = "RealTimeData";

    public void start() {
        SessionManager sessionManager = new SessionManager(context);
        FirebaseDatabase database = FirebaseDatabase.getInstance(AppConstants.getFirebaseRTDBUrl());
        String databaseUrl = AppConstants.getFirebaseRTDBRootRef() + sessionManager.getProviderID() + "/VIDEO_CALL";
        DatabaseReference databaseReference = database.getReference(databaseUrl);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                HashMap value = (HashMap) dataSnapshot.getValue();
                Timber.tag(TAG).d("Value is: " + value);

                if (value == null) return;
                Map<String, String> log = new HashMap<>();
                log.put("TAG", "CallListenerBackgroundService");
                log.put("action", "onDataChange");
                log.put("value", new Gson().toJson(value));
                FirebaseRealTimeDBUtils.logData(log);
                String device_token = String.valueOf(value.get("device_token"));
//                 if (!device_token.equals(refreshedFCMTokenID)) return;  // commented for ezazi only

                Bundle bundle = new Bundle();
                bundle.putString("doctorName", String.valueOf(value.get("doctorName")));
                bundle.putString("nurseId", String.valueOf(value.get("nurseId")));
                bundle.putString("roomId", String.valueOf(value.get("roomId")));
                bundle.putString("timestamp", String.valueOf(value.get("timestamp")));
                bundle.putString("actionType", "VIDEO_CALL");

                if (sessionManager.isLogout()) {
                    return;
                }

                Gson gson = new Gson();
                RtcArgs rtcArgs = gson.fromJson(gson.toJson(value), RtcArgs.class);
                if (rtcArgs == null) return;
                rtcArgs.setUrl("wss://ezazi.intelehealth.org:9090");
                rtcArgs.setSocketUrl(Constants.BASE_URL + "?userId=" + rtcArgs.getNurseId() + "&name=" + rtcArgs.getNurseId());
                rtcArgs.setActionType("VIDEO_CALL");

                RtcArgs previous = sessionManager.getRtcData();
                if (previous != null && Objects.equals(previous.getAppToken(), rtcArgs.getAppToken())) {
                    Timber.tag(TAG).d("Both are same => return");
                    return;
                }

                sessionManager.setRtcData(rtcArgs);

                boolean isOldNotification = false;
                if (value.containsKey("timestamp")) {
                    String timestamp = String.valueOf(value.get("timestamp"));

                    Date date = new Date();
                    if (timestamp != null) {
                        date.setTime(Long.parseLong(timestamp));
                        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormatter = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss"); //this format changeable
                        dateFormatter.setTimeZone(TimeZone.getDefault());

                        try {
                            Date ourDate = dateFormatter.parse(dateFormatter.format(date));
                            long seconds = 0;
                            if (ourDate != null) {
                                seconds = Math.abs(new Date().getTime() - ourDate.getTime()) / 1000;
                            }
                            Timber.tag(TAG).v("Current time - " + new Date());
                            Timber.tag(TAG).v("Notification time - " + ourDate);
                            Timber.tag(TAG).v("seconds - " + seconds);
                            if (seconds >= 30) {
                                isOldNotification = true;
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }
                if (!isOldNotification) {
                    Intent intent = new Intent(context, CallRTCNotifyReceiver.class);
                    intent.putExtras(bundle);
                    intent.putExtra(RtcUtilsKt.RTC_ARGS, rtcArgs);
                    intent.setAction("org.intelehealth.app.RTC_MESSAGE_EVENT");
                    context.sendBroadcast(intent);
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
                Timber.tag(TAG).w(error.toException(), "Failed to read value.");
            }
        });
    }
}
