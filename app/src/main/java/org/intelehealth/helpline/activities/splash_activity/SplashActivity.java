package org.intelehealth.helpline.activities.splash_activity;

import android.Manifest;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

import android.widget.Toast;


import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.List;
import java.util.Locale;

import org.intelehealth.helpline.R;
import org.intelehealth.helpline.activities.IntroActivity.IntroActivity;
import org.intelehealth.helpline.activities.homeActivity.HomeActivity;
import org.intelehealth.helpline.dataMigration.SmoothUpgrade;
import org.intelehealth.helpline.utilities.Logger;
import org.intelehealth.helpline.utilities.SessionManager;

import org.intelehealth.helpline.activities.loginActivity.LoginActivity;


public class SplashActivity extends AppCompatActivity {
    SessionManager sessionManager = null;
//    ProgressDialog TempDialog;
    int i = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_activity);
//        Getting App language through the session manager
        sessionManager = new SessionManager(SplashActivity.this);

      //  startService(new Intent(getBaseContext(), OnClearFromRecentService.class));
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
//                Timer t = new Timer();
//                t.schedule(new splash(), 2000);

//                TempDialog = new ProgressDialog(SplashActivity.this, R.style.AlertDialogStyle);
//                TempDialog.setMessage("Data migrating...");
//                TempDialog.setCancelable(false);
//                TempDialog.setProgress(i);
//                TempDialog.show();

                if (sessionManager.isMigration()) {
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                                nextActivity();
                        }
                    }, 2000);
                } else {
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //Do something after 100ms
                            SmoothUpgrade smoothUpgrade = new SmoothUpgrade(SplashActivity.this);
                            boolean smoothupgrade = smoothUpgrade.checkingDatabase();
                            if (smoothupgrade) {
//                                TempDialog.dismiss();
                                nextActivity();
                            } else {
//                                TempDialog.dismiss();
                                nextActivity();
                            }

                        }
                    }, 2000);
                }

            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
                Toast.makeText(SplashActivity.this, getString(R.string.permission_denied) + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
            }

        };
        TedPermission.with(this)
                .setPermissionListener(permissionlistener)
                .setDeniedMessage(R.string.reject_permission_results)
                .setPermissions(Manifest.permission.INTERNET,
                        Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.GET_ACCOUNTS,
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .check();
    }

    private void nextActivity() {
        boolean setup = sessionManager.isSetupComplete();

        String LOG_TAG = "SplashActivity";
        Logger.logD(LOG_TAG, String.valueOf(setup));
        if (setup) {

            if (sessionManager.isLogout()) {
                Logger.logD(LOG_TAG, "Starting login");
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finish();
            } else {
                Logger.logD(LOG_TAG, "Starting home");
                Intent intent = new Intent(this, HomeActivity.class);
                startActivity(intent);
                finish();
            }

        } else {
            Logger.logD(LOG_TAG, "Starting setup");
            Intent intent = new Intent(this, IntroActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onDestroy() {
//        TempDialog.dismiss();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
