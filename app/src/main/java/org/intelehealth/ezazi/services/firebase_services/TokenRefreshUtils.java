package org.intelehealth.ezazi.services.firebase_services;

import android.content.Context;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessaging;

import org.intelehealth.ezazi.app.IntelehealthApplication;

public class TokenRefreshUtils {
    private static final String TAG = TokenRefreshUtils.class.getName();

    public static void refreshToken(Context context) {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(token -> {
            if (token != null) IntelehealthApplication.getInstance().refreshedFCMTokenID = token;
            Log.d(TAG, "refreshToken: " + token);
        });
    }
}
