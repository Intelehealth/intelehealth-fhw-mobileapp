package org.intelehealth.unicef.utilities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public abstract class NetworkChangeListener extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        onNetworkChange(NetworkConnection.getConnectivityStatusString(context));
    }

    protected abstract void onNetworkChange(String[] status);
}
