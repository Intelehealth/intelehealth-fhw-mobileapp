package org.intelehealth.unicef.utilities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import org.intelehealth.unicef.R;

public class NetworkConnection {

    public static final String TYPE_WIFI = "WIFI";
    public static final String TYPE_MOBILE = "MOBILE_DATA";
    public static final String TYPE_NOT_CONNECTED = "NO_CONNECTION";

    public static boolean isOnline(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
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

    public static String[] getConnectivityStatusString(Context context) {
        String conn = NetworkConnection.getConnectionType(context);
        String status = null;
        String flag = "0"; // 0- not connected, 1- connected
        switch (conn) {
            case NetworkConnection.TYPE_WIFI:
            case NetworkConnection.TYPE_MOBILE:
                status = context.getString(R.string.internet_connected_txt);
                flag = "1";
                break;
            case NetworkConnection.TYPE_NOT_CONNECTED:
                status = context.getString(R.string.not_connected_txt);
                break;
        }
        return new String[]{status,flag};
    }
}


