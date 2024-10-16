package org.intelehealth.app.utilities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;

import org.intelehealth.app.utilities.CustomLog;

import androidx.core.content.ContextCompat;

public class NetworkUtils {
    Boolean isReceiverRegistered = false;
    Context context;
    NetworkChangeReceiver receiver;
    boolean isInternetAvailable;
    InternetCheckUpdateInterface listener;
    private boolean isConnected = false;

    public NetworkUtils(Context context) {
        this.context = context;
    }

    public NetworkUtils(Context context, InternetCheckUpdateInterface listener) {
        this.context = context;
        this.listener = listener;
    }

    public void callBroadcastReceiver() {
        try {
            if (context != null) {
                if (!isReceiverRegistered) {
                    IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
                    receiver = new NetworkChangeReceiver();
                    ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
                    isReceiverRegistered = true;
                }
            } else {
                CustomLog.d("TAG", "callBroadcastReceiver: context is null");
            }
        } catch (IllegalArgumentException e) {
            CustomLog.e("NetworkUtils", "BroadcastReceiver:" + e.getMessage(), e);
        }


    }

    public class NetworkChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            boolean result = isNetworkAvailable(context);

            if (listener != null) {
                listener.updateUIForInternetAvailability(result);
            }

        }

    }

    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivity.getActiveNetworkInfo();
        if (activeNetwork != null) {
            switch (activeNetwork.getType()) {
                case ConnectivityManager.TYPE_WIFI, ConnectivityManager.TYPE_MOBILE -> {
                    return true;
                }
                default -> {
                    return false;
                }
            }
        } else {
            return false;
        }

    }

    public void unregisterNetworkReceiver() {
        try {
            if (receiver != null) {
                context.unregisterReceiver(receiver);
            }
        } catch (IllegalArgumentException e) {
            CustomLog.e("NetworkUtils", "BroadcastReceiver: not registered=>" + e.getMessage(), e);
        }

    }

    public interface InternetCheckUpdateInterface {
        void updateUIForInternetAvailability(boolean isInternetAvailable);
    }

}
