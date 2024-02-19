package org.intelehealth.app.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.loginActivity.LoginActivityNew;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.syncModule.SyncUtils;

public class NavigationUtils {

    public void triggerSignOutOn401Response(Context context) {
        Toast.makeText(context, context.getString(R.string.your_session_has_expired_please_log_in_again), Toast.LENGTH_SHORT).show();
        OfflineLogin.getOfflineLogin().setOfflineLoginStatus(false);
        SyncUtils syncUtils = new SyncUtils();
        syncUtils.syncBackground();

        SessionManager sessionManager = new SessionManager(context);
        sessionManager.setReturningUser(false);
        sessionManager.setLogout(true);

        IntelehealthApplication.getInstance().disconnectSocket();
        Intent intent = new Intent(context, LoginActivityNew.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        if (context instanceof Activity activity) {
            activity.startActivity(intent);
            activity.finish();
        }
    }

}