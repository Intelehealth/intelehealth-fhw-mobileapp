package org.intelehealth.ekalarogya.activities.splash_activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.FirebaseApp;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.gson.Gson;

import org.intelehealth.ekalarogya.BuildConfig;
import org.intelehealth.ekalarogya.R;
import org.intelehealth.ekalarogya.activities.IntroActivity.IntroActivity;
import org.intelehealth.ekalarogya.activities.chooseLanguageActivity.ChooseLanguageActivity;
import org.intelehealth.ekalarogya.activities.homeActivity.HomeActivity;
import org.intelehealth.ekalarogya.activities.loginActivity.LoginActivity;
import org.intelehealth.ekalarogya.dataMigration.SmoothUpgrade;
import org.intelehealth.ekalarogya.services.firebase_services.TokenRefreshUtils;
import org.intelehealth.ekalarogya.utilities.Logger;
import org.intelehealth.ekalarogya.utilities.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class SplashActivity extends AppCompatActivity {
    private static final String TAG = "SplashActivity";
    SessionManager sessionManager = null;
    private static final int GROUP_PERMISSION_REQUEST = 1000;
    //    ProgressDialog TempDialog;
    int i = 5;

    @SuppressLint("SetTextI18n")
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

        if (isNetworkConnected()) // AEAT-552: // com.google.firebase.iid.GmsRpc.handleResponse -> java.io.IOException - SERVICE_NOT_AVAILABLE // this issue was happening coz internet was not connected and firebase token was trying to create leading to an exception and triggering an entry in crashlytics.
            TokenRefreshUtils.refreshToken(this);

        initFirebaseRemoteConfig();

        TextView versionName = findViewById(R.id.tvSplashAppVersionName);
        versionName.setText("Version: " + BuildConfig.VERSION_NAME);
    }

    private void initFirebaseRemoteConfig() {
        FirebaseApp.initializeApp(this);
        FirebaseRemoteConfig instance = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings
                .Builder()
                .setMinimumFetchIntervalInSeconds(300)
                .build();
        instance.setConfigSettingsAsync(configSettings);

        instance.fetchAndActivate().addOnCompleteListener(new OnCompleteListener<Boolean>() {
            @Override
            public void onComplete(@NonNull Task<Boolean> task) {
                if (task.isSuccessful() && !isFinishing()) {
                    long force_update_version_code = instance.getLong("force_update_version_code");
                    if (force_update_version_code > BuildConfig.VERSION_CODE) {
                        MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(SplashActivity.this);
                        alertDialogBuilder.setMessage(getString(R.string.warning_app_update));
                        alertDialogBuilder.setCancelable(false);
                        alertDialogBuilder.setPositiveButton(getString(R.string.generic_ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
                                } catch (android.content.ActivityNotFoundException anfe) {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
                                }
                                dialog.dismiss();
                                finish();
                            }
                        });
                        alertDialogBuilder.show();
                    } else {
                        checkPerm();
                    }
                } else {
                    checkPerm();
                }
            }
        });
    }

    private void checkPerm() {
        if (checkAndRequestPermissions()) {
            if (sessionManager.isMigration()) {
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //Do something after 100ms
                        SmoothUpgrade smoothUpgrade = new SmoothUpgrade(SplashActivity.this);
                        boolean smoothupgrade = smoothUpgrade.checkingDatabase();
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
//        PermissionListener permissionlistener = new PermissionListener() {
//
//            @Override
//            public void onPermissionGranted() {
////                Toast.makeText(SplashActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
////                Timer t = new Timer();
////                t.schedule(new splash(), 2000);
//
////                TempDialog = new ProgressDialog(SplashActivity.this, R.style.AlertDialogStyle);
////                TempDialog.setMessage("Data migrating...");
////                TempDialog.setCancelable(false);
////                TempDialog.setProgress(i);
////                TempDialog.show();
//
//                if (sessionManager.isMigration()) {
//                    final Handler handler = new Handler();
//                    handler.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            //Do something after 100ms
//                            SmoothUpgrade smoothUpgrade = new SmoothUpgrade(SplashActivity.this);
//                            boolean smoothupgrade = smoothUpgrade.checkingDatabase();
//                            nextActivity();
//                        }
//                    }, 2000);
//                } else {
//                    final Handler handler = new Handler();
//                    handler.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            //Do something after 100ms
//                            SmoothUpgrade smoothUpgrade = new SmoothUpgrade(SplashActivity.this);
//                            boolean smoothupgrade = smoothUpgrade.checkingDatabase();
//                            if (smoothupgrade) {
////                                TempDialog.dismiss();
//                                nextActivity();
//                            } else {
////                                TempDialog.dismiss();
//                                nextActivity();
//                            }
//
//                        }
//                    }, 2000);
//                }
//
//            }
//
//            @Override
//            public void onPermissionDenied(List<String> deniedPermissions) {
//                Toast.makeText(SplashActivity.this, getString(R.string.permission_denied) + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
//            }
//
//        };
//        TedPermission.with(this)
//                .setPermissionListener(permissionlistener)
//                .setDeniedMessage(R.string.reject_permission_results)
//                .setPermissions(Manifest.permission.INTERNET,
//                        Manifest.permission.ACCESS_NETWORK_STATE,
//                        Manifest.permission.GET_ACCOUNTS,
//                        Manifest.permission.CAMERA,
//                        Manifest.permission.READ_EXTERNAL_STORAGE,
//                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                        Manifest.permission.RECORD_AUDIO)
//                .check();
    }

    private void nextActivity() {
        boolean setup = sessionManager.isSetupComplete();

        String LOG_TAG = "SplashActivity";
        Logger.logD(LOG_TAG, String.valueOf(setup));
        if (sessionManager.isFirstTimeLaunch()) {
            Logger.logD(LOG_TAG, "Starting setup");
            Intent intent = new Intent(this, ChooseLanguageActivity.class);
            startActivity(intent);
            finish();
        } else {
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

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    private boolean checkAndRequestPermissions() {
        List<String> listPermissionsNeeded = new ArrayList<>();
        int cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int accessNetworkState = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE);
        int recordAudio = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        int getAccountPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS);
        int writeExternalStoragePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            writeExternalStoragePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES);
            int notificationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS);
            if (notificationPermission != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        if (accessNetworkState != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_NETWORK_STATE);
        }

        if (recordAudio != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.RECORD_AUDIO);
        }

        int phoneStatePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);


        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA);
        }
       /* if (getAccountPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.GET_ACCOUNTS);
        }*/
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
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == GROUP_PERMISSION_REQUEST) {
            boolean allGranted = grantResults.length != 0;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                checkPerm();
            } else {
                Log.e(TAG, "onRequestPermissionsResult: " + new Gson().toJson(permissions));
            }

        }
    }
}
