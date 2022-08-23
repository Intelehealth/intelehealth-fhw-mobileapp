package org.intelehealth.app.services.firebase_services;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.utilities.Logger;

public class TokenRefreshUtils {
    private static final String TAG = TokenRefreshUtils.class.getName();

    public static void refreshToken(Context context) {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Logger.logE(TAG, "getInstanceId failed", task.getException());
                            return;
                        }
                        if (task.getResult() == null) {
                            return;
                        }
                        // Get new Instance ID token
                        String token = task.getResult().getToken();
                        IntelehealthApplication.getInstance().refreshedFCMTokenID = token;

                        // Log and toast
                        Logger.logV(TAG, "FCM token: " + token);
                    }
                });
    }
}
