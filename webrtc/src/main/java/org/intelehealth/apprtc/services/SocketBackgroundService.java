package org.intelehealth.apprtc.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import org.intelehealth.apprtc.CompleteActivity;
import org.intelehealth.apprtc.data.Constants;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;

public class SocketBackgroundService extends Service {
    private static final String TAG = SocketBackgroundService.class.getName();
    private Socket mSocket;
    private String mFromUUId = "";
    private String mToUUId = "";
    private String mPatientUUid = "";
    private String mVisitUUID = "";
    private String mPatientName = "";

    public SocketBackgroundService(Context applicationContext) {
        super();
    }

    public SocketBackgroundService() {
    }

    public void setFromUserUUID(String fromUUID) {
        mFromUUId = fromUUID;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "onDestroy()!");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.v(TAG, "onStartCommand()!");

        return START_STICKY;
    }

    private void connectTOSocket() {
        try {
            mSocket = IO.socket(Constants.BASE_URL + "?userId=" + mFromUUId);
            mSocket.on("connect", args -> {
                Log.d(TAG, "connected!");
            });
            mSocket.on("disconnect", args -> {
                Log.d(TAG, "disconnected!");

            });
            mSocket.on("call", args -> {
                Log.d(TAG, "calling...");
                for (Object arg : args) {
                    Log.d(TAG, "call: " + String.valueOf(arg));
                }
                Intent in = new Intent(this, CompleteActivity.class);

                in.putExtra("roomId", mPatientUUid);
                in.putExtra("isInComingRequest", true);
                in.putExtra("doctorname", "Doctor");
                in.putExtra("nurseId", mFromUUId);
                startActivity(in);
            });


            mSocket.connect();

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
}