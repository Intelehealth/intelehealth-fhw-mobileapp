package org.intelehealth.ihutils.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

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
        if (context != null) {
            if (!isReceiverRegistered) {
                IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
                receiver = new NetworkChangeReceiver();
                ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
                isReceiverRegistered = true;
            }
        } else {
            Log.d("TAG", "callBroadcastReceiver: context is null");
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
        if (receiver != null) {
            context.unregisterReceiver(receiver);
        }
    }

    public interface InternetCheckUpdateInterface {
        void updateUIForInternetAvailability(boolean isInternetAvailable);
    }

}
