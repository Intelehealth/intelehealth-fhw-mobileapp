package org.intelehealth.app.ui2.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;


public class CheckInternetAvailability {
    private static boolean isConnected = false;
    Context context;

    public CheckInternetAvailability(Context context) {
        this.context = context;
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
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

}
