package com.shivam.androidwebrtc.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class SocketBackgroundService extends Service {
    //private SocketThread mSocketThread;
    public SocketBackgroundService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}