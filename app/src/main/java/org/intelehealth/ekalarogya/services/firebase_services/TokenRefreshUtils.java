package org.intelehealth.ekalarogya.services.firebase_services;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.intelehealth.ekalarogya.app.IntelehealthApplication;
import org.intelehealth.ekalarogya.utilities.Logger;

public class TokenRefreshUtils {
    private static final String TAG = TokenRefreshUtils.class.getName();

    public static void refreshToken(Context context) {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
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
