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
        if(context!=null){
            if (!isReceiverRegistered) {
                IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
                receiver = new NetworkChangeReceiver();
                //context.registerReceiver(receiver, filter);
                ContextCompat.registerReceiver(this.context, receiver, filter, ContextCompat.RECEIVER_EXPORTED); //changed because previous code not working on android 14 and above

                isReceiverRegistered = true;
            }
        }else{
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
        int flag = 0;

        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {

                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        if (!isConnected) {
                            flag = 1;
                            isInternetAvailable = true;

                        }
                    }
                }
            }
        }

        if (flag == 0) {
            isInternetAvailable = false;

        }
        return isInternetAvailable;

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
