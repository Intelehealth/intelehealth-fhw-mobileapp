package org.intelehealth.ekalarogya.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import org.intelehealth.ekalarogya.R;
import org.intelehealth.ekalarogya.activities.loginActivity.LoginActivity;
import org.intelehealth.ekalarogya.app.IntelehealthApplication;
import org.intelehealth.ekalarogya.syncModule.SyncUtils;

public class NavigationUtils {

    public void triggerSignOutOn401Response(Context context) {
        Toast.makeText(context, context.getString(R.string.session_expired_please_sign_in_again), Toast.LENGTH_SHORT).show();
        OfflineLogin.getOfflineLogin().setOfflineLoginStatus(false);
        SyncUtils syncUtils = new SyncUtils();
        syncUtils.syncBackground();

        SessionManager sessionManager = new SessionManager(context);
        sessionManager.setReturningUser(false);
        sessionManager.setUserProfileDetail("");
        sessionManager.setLogout(true);

        IntelehealthApplication.getInstance().stopRealTimeObserverAndSocket();
        Intent intent = new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        if (context instanceof Activity activity) {
            activity.startActivity(intent);
            activity.finish();
        }
    }

}