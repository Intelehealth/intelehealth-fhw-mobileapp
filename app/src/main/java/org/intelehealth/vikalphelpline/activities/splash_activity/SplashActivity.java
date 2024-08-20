package org.intelehealth.vikalphelpline.activities.splash_activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.widget.Toast;


import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.intelehealth.vikalphelpline.R;
import org.intelehealth.vikalphelpline.activities.IntroActivity.IntroActivity;
import org.intelehealth.vikalphelpline.activities.homeActivity.HomeActivity;
import org.intelehealth.vikalphelpline.dataMigration.SmoothUpgrade;
import org.intelehealth.vikalphelpline.utilities.DialogUtils;
import org.intelehealth.vikalphelpline.utilities.Logger;
import org.intelehealth.vikalphelpline.utilities.SessionManager;

import org.intelehealth.vikalphelpline.activities.loginActivity.LoginActivity;


public class SplashActivity extends AppCompatActivity {
    private static final int GROUP_PERMISSION_REQUEST = 1000;
    public static final int PERMISSION_USAGE_ACCESS_STATS = 2792;
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
        if (checkAndRequestPermissions()) {
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
      /*  PermissionListener permissionlistener = new PermissionListener() {

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
        TedPermission.create()
                .setPermissionListener(permissionlistener)
                .setDeniedMessage(R.string.reject_permission_results)
                .setPermissions(Manifest.permission.INTERNET,
                        Manifest.permission.ACCESS_NETWORK_STATE,
//                        Manifest.permission.GET_ACCOUNTS,
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE
//                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                .check();*/
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == GROUP_PERMISSION_REQUEST) {
            boolean allGranted = grantResults.length != 0;
            boolean permissionsCheck = false;
            for (int grantResult : grantResults) {
                permissionsCheck = true;
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (permissionsCheck) {
                if (allGranted) {
                    checkPerm();
                } else {
                    Toast.makeText(SplashActivity.this, getString(R.string.permission_denied) + Arrays.toString(permissions), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    private boolean checkAndRequestPermissions() {
        List<String> listPermissionsNeeded = new ArrayList<>();
        int cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int getAccountPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS);
        int writeExternalStoragePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            writeExternalStoragePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES);
            int notificationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS);
            if (notificationPermission != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            int fullScreenIntent = ContextCompat.checkSelfPermission(this, Manifest.permission.USE_FULL_SCREEN_INTENT);
            if (fullScreenIntent != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.USE_FULL_SCREEN_INTENT);
            }
        }
        int phoneStatePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);


        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA);
        }
        if (getAccountPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.GET_ACCOUNTS);
        }
        if (writeExternalStoragePermission != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                listPermissionsNeeded.add(Manifest.permission.READ_MEDIA_IMAGES);
            } else {
                listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
                listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }
        if (phoneStatePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), GROUP_PERMISSION_REQUEST);
            return false;
        }

//        checkOverlayPermission();
        return true;
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
