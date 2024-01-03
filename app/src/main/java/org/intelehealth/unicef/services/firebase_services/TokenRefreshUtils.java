package org.intelehealth.unicef.services.firebase_services;

import android.content.Context;

import androidx.annotation.NonNull;


import org.intelehealth.unicef.app.IntelehealthApplication;
import org.intelehealth.unicef.utilities.Logger;

public class TokenRefreshUtils {
    private static final String TAG = TokenRefreshUtils.class.getName();

    public static void refreshToken(Context context) {
//        FirebaseInstanceId.getInstance().getInstanceId()
//                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
//                        if (!task.isSuccessful()) {
//                            Logger.logE(TAG, "getInstanceId failed", task.getException());
//                            return;
//                        }
//                        if (task.getResult() == null) {
//                            return;
//                        }
//                        // Get new Instance ID token
//                        String token = task.getResult().getToken();
//                        IntelehealthApplication.getInstance().refreshedFCMTokenID = token;
//
//                        // Log and toast
//                        Logger.logV(TAG, "FCM token: " + token);
//                    }
//                });
    }
}
