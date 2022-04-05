package org.intelehealth.unicef.utilities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public abstract class NetworkChangeListener extends BroadcastReceiver {

    // Default no-args constructor added to avoid bug of no-instance exception - Prajwal
    public NetworkChangeListener() {
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        try {
            switch (intent.getAction()) {
                case "android.net.conn.CONNECTIVITY_CHANGE":
                case "android.net.wifi.WIFI_STATE_CHANGED":
                    onNetworkChange(NetworkConnection.getConnectivityStatusString(context));
                    break;
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    protected abstract void onNetworkChange(String[] status);
}
