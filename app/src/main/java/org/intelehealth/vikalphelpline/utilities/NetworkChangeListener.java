package org.intelehealth.vikalphelpline.utilities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public abstract class NetworkChangeListener extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, final Intent intent) {
        String status = NetworkConnection.getConnectivityStatusString(context);
        onNetworkChange(status);
    }

    protected abstract void onNetworkChange(String status);
}
