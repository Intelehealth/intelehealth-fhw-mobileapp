package org.intelehealth.ezazi.services.firebase_services;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import org.intelehealth.ezazi.app.IntelehealthApplication;

public class TokenRefreshUtils {
    private static final String TAG = TokenRefreshUtils.class.getName();

    public static void refreshToken(Context context) {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                IntelehealthApplication.getInstance().refreshedFCMTokenID = task.getResult();
                Log.d(TAG, "refreshToken: " + task.getResult());
            }
        });
//        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(token -> {
//
//        });
    }
}
