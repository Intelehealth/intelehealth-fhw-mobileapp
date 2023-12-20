package org.intelehealth.unicef.activities.chooseLanguageActivity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.Slide;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import org.intelehealth.fcm.utils.FcmRemoteConfig;
import org.intelehealth.fcm.utils.FcmTokenGenerator;
import org.intelehealth.unicef.BuildConfig;
import org.intelehealth.unicef.R;
import org.intelehealth.unicef.activities.IntroActivity.IntroScreensActivity_New;
import org.intelehealth.unicef.activities.achievements.fragments.MyAchievementsFragment;
import org.intelehealth.unicef.activities.base.LocalConfigActivity;
import org.intelehealth.unicef.activities.homeActivity.HomeScreenActivity_New;
import org.intelehealth.unicef.activities.loginActivity.LoginActivityNew;
import org.intelehealth.unicef.activities.onboarding.SetupPrivacyNoteActivity_New;
import org.intelehealth.unicef.app.IntelehealthApplication;
import org.intelehealth.unicef.dataMigration.SmoothUpgrade;
import org.intelehealth.unicef.utilities.DialogUtils;
import org.intelehealth.unicef.utilities.Logger;
import org.intelehealth.unicef.utilities.SessionManager;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class SplashScreenActivity extends LocalConfigActivity implements SplashLanguageUpdater {
    private static final String TAG = "SplashScreenActivity";
    RecyclerView rvSelectLanguage;
    View layoutLanguage;
    ViewGroup layoutParent;
    ConstraintLayout layoutHeader;
    String appLanguage;
    SessionManager sessionManager = null;
    private static final int GROUP_PERMISSION_REQUEST = 1000;
    String LOG_TAG = "SplashActivity";
    BiometricPrompt biometricPrompt;
    BiometricPrompt.PromptInfo promptInfo;

    private TextView tvSelectLanguage, tvTitle;
    private Button btnNextToIntro;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //        Getting App language through the session manager
        sessionManager = new SessionManager(this);
        //  startService(new Intent(getBaseContext(), OnClearFromRecentService.class));
        String appLanguage = sessionManager.getAppLanguage();
        if (!appLanguage.equalsIgnoreCase("")) {
            super.setLocale(appLanguage);
        }

        setContentView(R.layout.activity_splash_screenactivity_ui2);
        context = SplashScreenActivity.this;

        rvSelectLanguage = findViewById(R.id.rv_select_language);
        layoutLanguage = findViewById(R.id.layout_panel);
        layoutParent = findViewById(R.id.layout_parent);
        layoutHeader = findViewById(R.id.layout_child1);
        tvSelectLanguage = findViewById(R.id.textView8);
        tvTitle = findViewById(R.id.tv_title);

        // refresh the fcm token
        FcmTokenGenerator.INSTANCE.getDeviceToken(token -> {
            IntelehealthApplication.getInstance().refreshedFCMTokenID = token;
            return Unit.INSTANCE;
        });

        FcmRemoteConfig.getRemoteConfig(this, fcmConfigExecutor);
    }

    private void checkAndSetLanguage() {
        if (sessionManager.isFirstTimeLaunch()) {
            animateViews();
            populatingLanguages();
            saveLanguage();
        } else {
            nextActivity();
        }
    }

    private void saveLanguage() {
        appLanguage = sessionManager.getAppLanguage();
        if (!appLanguage.equalsIgnoreCase("")) {
            setLocale(appLanguage);
        }

        btnNextToIntro = findViewById(R.id.btn_next_to_intro);
        btnNextToIntro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLocale(sessionManager.getAppLanguage());
                if (sessionManager.isFirstTimeLaunch()) {
                    Intent intent = new Intent(SplashScreenActivity.this, IntroScreensActivity_New.class);
                    startActivity(intent);
                    //sessionManager.setFirstTimeLaunch(false);
                } else {
                /*    Intent intent = new Intent(SplashScreenActivity.this, HomeScreenActivity_New.class);
                    intent.putExtra("from", "splash");
                    intent.putExtra("username", "");
                    intent.putExtra("password", "");
                    startActivity(intent);*/

                    nextActivity();
                }
                finish(); // TODO: uncomment
                // testing...
               /* sessionManager.setServerUrl(AppConstants.DEMO_URL);
                Intent intent = new Intent(SplashScreenActivity.this, HomeScreenActivity_New.class);
                startActivity(intent); // TODO: remove this block code later.*/
            }
        });

    }

    private final Function1<FirebaseRemoteConfig, Unit> fcmConfigExecutor = firebaseRemoteConfig -> {
        checkLatestVersionUpdate(firebaseRemoteConfig);
        return Unit.INSTANCE;
    };

    private void checkLatestVersionUpdate(FirebaseRemoteConfig config) {
        long forceUpdateVersionCode = config.getLong("force_update_version_code");
        if (forceUpdateVersionCode > BuildConfig.VERSION_CODE) {
            MyAchievementsFragment.CustomDialog customDialog = new MyAchievementsFragment.CustomDialog(context);
            customDialog.showDialog1(getResources().getString(R.string.app_update_available), getResources().getString(R.string.warning_app_update), action -> {
                if (action == DialogUtils.CustomDialogListener.POSITIVE_CLICK) {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
                    } catch (android.content.ActivityNotFoundException anfe) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
                    }
                    finish();
                }
            });
        } else {
            checkPerm();
        }
    }

//    private void initFirebaseRemoteConfig() {
//        FcmRemoteConfig.getRemoteConfig(this, new Function1<FirebaseRemoteConfig, Unit>() {
//            @Override
//            public Unit invoke(FirebaseRemoteConfig firebaseRemoteConfig) {
//                return Unit.INSTANCE;
//            }
//        });
//        FirebaseApp.initializeApp(this);
//        FirebaseRemoteConfig instance = FirebaseRemoteConfig.getInstance();
//        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder().setMinimumFetchIntervalInSeconds(0).build();
//        instance.setConfigSettingsAsync(configSettings);
//
//        instance.fetchAndActivate().addOnCompleteListener(new OnCompleteListener<Boolean>() {
//            @Override
//            public void onComplete(@NonNull Task<Boolean> task) {
//                if (task.isSuccessful() && !isFinishing()) {
//                    long force_update_version_code = instance.getLong("force_update_version_code");
//                    if (force_update_version_code > BuildConfig.VERSION_CODE) {
//                        MyAchievementsFragment.CustomDialog customDialog = new MyAchievementsFragment.CustomDialog(context);
//                        customDialog.showDialog1(getResources().getString(R.string.app_update_available), getResources().getString(R.string.warning_app_update), action -> {
//                            if (action == DialogUtils.CustomDialogListener.POSITIVE_CLICK) {
//                                try {
//                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
//                                } catch (android.content.ActivityNotFoundException anfe) {
//                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
//                                }
//                                finish();
//                            }
//                        });
//                    } else {
//                        checkPerm();
//                    }
//                } else {
//                    checkPerm();
//                }
//            }
//        });
//    }

    private boolean checkAndRequestPermissions() {
        int cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int getAccountPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS);
        int phoneStatePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
        List<String> listPermissionsNeeded = new ArrayList<>();

        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA);
        }
        if (getAccountPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.GET_ACCOUNTS);
        }
        if (phoneStatePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_PHONE_STATE);
        }

        // POST NOTIFICATIONS permission is required only after API 33
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            int notificationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS);
            if (notificationPermission != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.POST_NOTIFICATIONS);
            }
        }


        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), GROUP_PERMISSION_REQUEST);
            return false;
        }
        return true;
    }

    private void checkPerm() {
        if (checkAndRequestPermissions()) {
            if (sessionManager.isMigration()) {
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() { //Do something after 100ms
                        checkAndSetLanguage();
                    }
                }, 2000);
            } else {
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() { //Do something after 100ms
                        SmoothUpgrade smoothUpgrade = new SmoothUpgrade(SplashScreenActivity.this);
                        boolean smoothupgrade = smoothUpgrade.checkingDatabase();
                        if (smoothupgrade) {
                            checkAndSetLanguage();
                        }
                    }
                }, 2000);
            }
        }
//        else
//        {
//            checkPerm();
//        }
       /* PermissionListener permissionlistener = new PermissionListener() {

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


            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
                Toast.makeText(SplashActivity.this, getString(R.string.permission_denied) + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
            }

        };
        TedPermission.with(this)
                .setPermissionListener(permissionlistener)
                .setDeniedMessage(R.string.reject_permission_results)
                .setPermissions(*//*Manifest.permission.INTERNET,
                        Manifest.permission.ACCESS_NETWORK_STATE,*//*
                        Manifest.permission.GET_ACCOUNTS,
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .check();*/
    }

    private void nextActivity() {

        boolean setup = sessionManager.isSetupComplete();

        String LOG_TAG = "SplashActivity";
        Logger.logD(LOG_TAG, String.valueOf(setup));
        if (sessionManager.isFirstTimeLaunch()) {
            Logger.logD(LOG_TAG, "Starting setup");
            Intent intent = new Intent(this, IntroScreensActivity_New.class);
            startActivity(intent);
            finish();
        } else {
            if (setup) {

                /*if (sessionManager.isLogout()) {
                    Logger.logD(LOG_TAG, "Starting login");
                    Intent intent = new Intent(this, LoginActivityNew.class);
                    startActivity(intent);
                    finish();
                } else {
                    Logger.logD(LOG_TAG, "Starting home");
                    Intent intent = new Intent(this, HomeScreenActivity_New.class);
                    intent.putExtra("from", "splash");
                    intent.putExtra("username", "");
                    intent.putExtra("password", "");
                    startActivity(intent);
                    finish();
                }*/

                if (sessionManager.isEnableAppLock()) fingerPrintAuthenticate();
                else navigateToNextActivity();

            } else {
                Logger.logD(LOG_TAG, "Starting setup");
                Intent intent = new Intent(this, SetupPrivacyNoteActivity_New.class);
                startActivity(intent);
                finish();
            }

        }
    }

    private void animateViews() {
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Animation translateAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.ui2_new_center_to_top);
                translateAnim.setFillAfter(true);
                translateAnim.setFillEnabled(true);
                translateAnim.setFillBefore(false);
                translateAnim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        showChooseLanguageUI(true);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                layoutHeader.startAnimation(translateAnim);

            }
        }, 500);


    }

    private void showChooseLanguageUI(boolean show) {
        Transition transition = new Slide(Gravity.BOTTOM);
        transition.setDuration(2000);
        transition.addTarget(R.id.layout_panel);

        TransitionManager.beginDelayedTransition(layoutParent, transition);
        layoutLanguage.setVisibility(show ? View.VISIBLE : View.GONE);

    }

    public void populatingLanguages() {
        try {
            List<JSONObject> itemList = new ArrayList<JSONObject>();

            JSONObject jsonObject = new JSONObject();

            jsonObject = new JSONObject();
            jsonObject.put("name", "English");
            jsonObject.put("code", "en");
            jsonObject.put("selected", sessionManager.getAppLanguage().isEmpty() || sessionManager.getAppLanguage().equalsIgnoreCase("en"));
            itemList.add(jsonObject);

            jsonObject = new JSONObject();
            jsonObject.put("name", "русский");
            jsonObject.put("code", "ru");
            jsonObject.put("selected", sessionManager.getAppLanguage().equalsIgnoreCase("ru"));
            itemList.add(jsonObject);

            ChooseLanguageAdapterNew languageListAdapter = new ChooseLanguageAdapterNew(SplashScreenActivity.this, itemList, (jsonObject1, index) -> {
                try {
                    sessionManager.setAppLanguage(jsonObject1.getString("code"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, this);

            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
            rvSelectLanguage.setLayoutManager(layoutManager);
            rvSelectLanguage.setItemAnimator(new DefaultItemAnimator());
            rvSelectLanguage.setAdapter(languageListAdapter);


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLanguageSelected(String language) {
        Configuration configuration = getResources().getConfiguration();
        configuration = new Configuration(configuration);
        configuration.setLocale(new Locale(language));
        Context localizedContext = createConfigurationContext(configuration);

        tvTitle.setText(localizedContext.getText(R.string.splash_desc));
        tvSelectLanguage.setText(localizedContext.getText(R.string.select_language));
        btnNextToIntro.setText(localizedContext.getText(R.string.next_new));
    }

    public interface ItemSelectionListener {
        void onSelect(JSONObject jsonObject, int index);
    }

    public void setLocale(String appLanguage) {
        // here comes en, hi, mr
        Locale locale = new Locale(appLanguage);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
    }

    private void fingerPrintAuthenticate() {
        BiometricManager biometricManager = BiometricManager.from(this);
        switch (biometricManager.canAuthenticate()) {
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_fingerprint_sensor), Toast.LENGTH_SHORT).show();
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.fingerprint_not_working), Toast.LENGTH_SHORT).show();
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_fingerprint_assigned), Toast.LENGTH_SHORT).show();
                break;
        }

        Executor executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(SplashScreenActivity.this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.login_failed), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.login_successfully), Toast.LENGTH_SHORT).show();
                navigateToNextActivity();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.login_failed), Toast.LENGTH_SHORT).show();
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder().setTitle(getResources().getString(R.string.intelehealth_login)).setSubtitle(getResources().getString(R.string.touch_fingerprint)).setDeviceCredentialAllowed(true).build();

        biometricPrompt.authenticate(promptInfo);

    }

    private void navigateToNextActivity() {
        if (sessionManager.isLogout()) {
            Logger.logD(TAG, "Starting login");
            Intent intent = new Intent(this, LoginActivityNew.class);
            startActivity(intent);
            finish();
        } else {
            Logger.logD(TAG, "Starting home");
            Intent intent = new Intent(this, HomeScreenActivity_New.class);
            intent.putExtra("from", "splash");
            intent.putExtra("username", "");
            intent.putExtra("password", "");
            startActivity(intent);
            finish();
        }
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
                showPermissionDeniedAlert(permissions);
            }

        }
    }

    private void showPermissionDeniedAlert(String[] permissions) {
        MaterialAlertDialogBuilder alertdialogBuilder = new MaterialAlertDialogBuilder(this);

        // AlertDialog.Builder alertdialogBuilder = new AlertDialog.Builder(this, R.style.AlertDialogStyle);
        alertdialogBuilder.setMessage(R.string.reject_permission_results);
        alertdialogBuilder.setPositiveButton(R.string.retry_again, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                checkPerm();
            }
        });
        alertdialogBuilder.setNegativeButton(R.string.ok_close_now, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });

        AlertDialog alertDialog = alertdialogBuilder.create();
        alertDialog.show();

        Button positiveButton = alertDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE);
        Button negativeButton = alertDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE);

        positiveButton.setTextColor(getResources().getColor(R.color.colorPrimary));
        //positiveButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        negativeButton.setTextColor(getResources().getColor(R.color.colorPrimary));
        //negativeButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        IntelehealthApplication.setAlertDialogCustomTheme(this, alertDialog);
    }

}