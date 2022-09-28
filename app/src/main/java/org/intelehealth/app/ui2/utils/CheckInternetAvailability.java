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
        int flag = 0;
        boolean result = false;

        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {

                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        if (!isConnected) {
                            flag = 1;
                            result = true;
                        }
                    }
                }
            }
        }

        if (flag == 0) {
            result = false;
        }
        return result;
    }

}
