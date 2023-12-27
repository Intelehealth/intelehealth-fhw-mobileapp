package org.intelehealth.ekalarogya.services.firebase_services;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;


import com.google.firebase.messaging.FirebaseMessaging;

import org.intelehealth.ekalarogya.app.IntelehealthApplication;
import org.intelehealth.ekalarogya.utilities.Logger;

public class TokenRefreshUtils {
    private static final String TAG = TokenRefreshUtils.class.getName();

    public static void refreshToken(Context context) {
        FirebaseMessaging.getInstance().setAutoInitEnabled(true);
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                IntelehealthApplication.getInstance().refreshedFCMTokenID = task.getResult();
                Logger.logV(TAG, "FCM token: " + task.getResult());
            } else {
                new Handler(Looper.getMainLooper()).postDelayed(() -> refreshToken(context), 500);
            }
        });
    }
}
