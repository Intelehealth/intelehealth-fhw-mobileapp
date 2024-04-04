package org.intelehealth.app.utilities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;

public class NetworkConnection {

    public static final String TYPE_WIFI = "WIFI";
    public static final String TYPE_MOBILE = "MOBILE_DATA";
    public static final String TYPE_NOT_CONNECTED = "NO_CONNECTION";

    public static boolean isOnline(Context context) {
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

    public static boolean isCapableNetwork(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
        if (activeNetwork != null) {
            switch (activeNetwork.getType()) {
                case ConnectivityManager.TYPE_WIFI, ConnectivityManager.TYPE_MOBILE -> {
                    return capabilities != null
                            && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                            && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
                }
                default -> {
                    return false;
                }
            }
        } else {
            return false;
        }
    }


    public static boolean isConnecting(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }


    public static String getConnectionType(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) return TYPE_WIFI;
            if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) return TYPE_MOBILE;
        }
        return TYPE_NOT_CONNECTED;
    }

    public static String getConnectivityStatusString(Context context) {
        String conn = NetworkConnection.getConnectionType(context);
        String status = null;
        switch (conn) {
            case NetworkConnection.TYPE_WIFI:
                status = "Connected to Internet!";
                break;
            case NetworkConnection.TYPE_MOBILE:
                status = "Connected to Internet!";
                break;
            case NetworkConnection.TYPE_NOT_CONNECTED:
                status = "Not connected to Internet!";
                break;
        }
        return status;
    }
}


