package io.intelehealth.client.views.activites;

import android.Manifest;
import android.content.Intent;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import io.intelehealth.client.R;
import io.intelehealth.client.databinding.ActivitySplashActivityBinding;
import io.intelehealth.client.utilities.Logger;
import io.intelehealth.client.utilities.SessionManager;


public class SplashActivity extends AppCompatActivity {
    SessionManager sessionManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_splash_activity);
        ActivitySplashActivityBinding activitySplashActivityBinding = DataBindingUtil.setContentView(this, R.layout.activity_splash_activity);
        sessionManager = new SessionManager(SplashActivity.this);

        String appLanguage = sessionManager.getAppLanguage();

        if (!appLanguage.equalsIgnoreCase("")) {
            Locale locale = new Locale(appLanguage);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        }
        checkPerm();
    }

    private void checkPerm() {
        PermissionListener permissionlistener = new PermissionListener() {

            @Override
            public void onPermissionGranted() {
//                Toast.makeText(SplashActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
                Timer t = new Timer();
                t.schedule(new splash(), 3000);
            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
                Toast.makeText(SplashActivity.this, "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
            }

        };
        TedPermission.with(this)
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .setPermissions(Manifest.permission.INTERNET,
                        Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.GET_ACCOUNTS,
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.SEND_SMS)
                .check();
    }

    private void nextActivity() {
        boolean setup = sessionManager.isSetupComplete();

        String LOG_TAG = "SplashActivity";
        Logger.logD(LOG_TAG, String.valueOf(setup));
        if (setup) {
            Logger.logD(LOG_TAG, "Starting login");
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            Logger.logD(LOG_TAG, "Starting setup");
            Intent intent = new Intent(this, SetupActivity.class);
            startActivity(intent);
            finish();
        }
    }

    class splash extends TimerTask {
        @Override
        public void run() {
            nextActivity();
        }
    }
}
