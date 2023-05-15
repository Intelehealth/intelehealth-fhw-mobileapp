package org.intelehealth.unicef.activities.splash_activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.multidex.MultiDex;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.FirebaseApp;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import org.intelehealth.unicef.BuildConfig;
import org.intelehealth.unicef.R;
import org.intelehealth.unicef.activities.IntroActivity.IntroActivity;
import org.intelehealth.unicef.activities.chooseLanguageActivity.ChooseLanguageActivity;
import org.intelehealth.unicef.activities.homeActivity.HomeActivity;
import org.intelehealth.unicef.activities.loginActivity.LoginActivity;
import org.intelehealth.unicef.dataMigration.SmoothUpgrade;
import org.intelehealth.unicef.services.firebase_services.TokenRefreshUtils;
import org.intelehealth.unicef.utilities.Logger;
import org.intelehealth.unicef.utilities.NetworkConnection;
import org.intelehealth.unicef.utilities.SessionManager;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;


public class SplashActivity extends AppCompatActivity {
    SessionManager sessionManager = null;
    String LOG_TAG = "SplashActivity";
    //    ProgressDialog TempDialog;
    int i = 5;

    BiometricPrompt biometricPrompt;
    BiometricPrompt.PromptInfo promptInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MultiDex.install(this);
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

        // If during refreshing/initiating firebase token; if internet is off in that case the non-fatal event was observed - Prajwal
        if (NetworkConnection.isOnline(getApplication())) {
            // refresh the fcm token
            TokenRefreshUtils.refreshToken(this);
            initFirebaseRemoteConfig();
        }
    }

    private void initFirebaseRemoteConfig() {
        FirebaseApp.initializeApp(this);
        FirebaseRemoteConfig instance = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(0)
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
                        public void run() { //Do something after 100ms
                            nextActivity();
                        }
                    }, 2000);
                } else {
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() { //Do something after 100ms
                            SmoothUpgrade smoothUpgrade = new SmoothUpgrade(SplashActivity.this);
                            boolean smoothupgrade = smoothUpgrade.checkingDatabase();
                            if (smoothupgrade) {
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
                        Manifest.permission.CAMERA)
                .check();
    }

    private void nextActivity() {
        boolean setup = sessionManager.isSetupComplete();
//        String LOG_TAG = "SplashActivity";
        Logger.logD(LOG_TAG, String.valueOf(setup));

        if (sessionManager.isFirstTimeLaunch()) {
            Logger.logD(LOG_TAG, "Starting setup");
            Intent intent = new Intent(this, ChooseLanguageActivity.class);
            startActivity(intent);
            finish();
        } else {
            if (setup) {
                /*if (sessionManager.isLogout()) {
                    Logger.logD(LOG_TAG, "Starting login");
                    Intent intent = new Intent(this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Logger.logD(LOG_TAG, "Starting home");
                    Intent intent = new Intent(this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                }*/
                if (sessionManager.isEnableAppLock())
                    fingerPrintAuthenticate();
                else
                    navigateToNextActivity();

            } else {
                Logger.logD(LOG_TAG, "Starting setup");
                Intent intent = new Intent(this, IntroActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }

    private void fingerPrintAuthenticate() {
        BiometricManager biometricManager = BiometricManager.from(this);
        switch (biometricManager.canAuthenticate()) {
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Toast.makeText(getApplicationContext(), "Device doesn't have fingerprint sensor.", Toast.LENGTH_SHORT).show();
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Toast.makeText(getApplicationContext(), "Device fingerprint sensor is not working.", Toast.LENGTH_SHORT).show();
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                Toast.makeText(getApplicationContext(), "Device fingerprint sensor has no fingerprint assigned.", Toast.LENGTH_SHORT).show();
                break;
        }

        Executor executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(SplashActivity.this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(getApplicationContext(), "Log in failed, please try again later.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Toast.makeText(getApplicationContext(), "Successfully logged in!", Toast.LENGTH_SHORT).show();
                navigateToNextActivity();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(getApplicationContext(), "Log in failed, please try again later.", Toast.LENGTH_SHORT).show();
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Intelehealth Login")
                .setSubtitle("Touch fingerprint sensor to login")
                .setDeviceCredentialAllowed(true)
                .build();

        biometricPrompt.authenticate(promptInfo);

    }

    private void navigateToNextActivity() {
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
