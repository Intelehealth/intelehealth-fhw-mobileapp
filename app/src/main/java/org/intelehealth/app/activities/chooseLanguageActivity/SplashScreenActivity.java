package org.intelehealth.app.activities.chooseLanguageActivity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.LocaleList;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.FirebaseApp;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import org.intelehealth.app.BuildConfig;
import org.intelehealth.app.R;
import org.intelehealth.app.activities.IntroActivity.IntroScreensActivity_New;
import org.intelehealth.app.activities.homeActivity.HomeScreenActivity_New;
import org.intelehealth.app.activities.loginActivity.LoginActivityNew;
import org.intelehealth.app.activities.onboarding.SetupPrivacyNoteActivity_New;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.dataMigration.SmoothUpgrade;
import org.intelehealth.app.services.firebase_services.TokenRefreshUtils;
import org.intelehealth.app.utilities.DialogUtils;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.SessionManager;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;

public class SplashScreenActivity extends AppCompatActivity {
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
private View emptyView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screenactivity_ui2);
        sessionManager = new SessionManager(SplashScreenActivity.this);
        rvSelectLanguage = findViewById(R.id.rv_select_language);
        layoutLanguage = findViewById(R.id.layout_panel);
        layoutParent = findViewById(R.id.layout_parent);
        layoutHeader = findViewById(R.id.layout_child1);
        emptyView = findViewById(R.id.empty_view);


        //  startService(new Intent(getBaseContext(), OnClearFromRecentService.class));
        String appLanguage = sessionManager.getAppLanguage();
        if (!appLanguage.equalsIgnoreCase("")) {
            Locale locale = new Locale(appLanguage);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        }
        // refresh the fcm token
        TokenRefreshUtils.refreshToken(this);
        initFirebaseRemoteConfig();

        if (sessionManager.isFirstTimeLaunch()) {
            checkPerm();
            animateViews();
            populatingLanguages();
        } else {

            //as we are implementing force update now thus commenting this.
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    nextActivity();
                }
            }, 3000);
        }

        saveLanguage();

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(setLocale(newBase));
    }

    public Context setLocale(Context context) {
        SessionManager sessionManager1 = new SessionManager(context);
        String appLanguage = sessionManager1.getAppLanguage();
        Resources res = context.getResources();
        Configuration conf = res.getConfiguration();
        Locale locale = new Locale(appLanguage);
        Locale.setDefault(locale);
        conf.setLocale(locale);
        context.createConfigurationContext(conf);
        DisplayMetrics dm = res.getDisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            conf.setLocales(new LocaleList(locale));
        } else {
            conf.locale = locale;
        }
        res.updateConfiguration(conf, dm);
        return context;
    }

    private void saveLanguage() {
        appLanguage = sessionManager.getAppLanguage();
        if (!appLanguage.equalsIgnoreCase("")) {
            setLocale(appLanguage);
        }

        Button btnNextToIntro = findViewById(R.id.btn_next_to_intro);
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

//                    nextActivity();
                }
                finish(); // TODO: uncomment
                // testing...
               /* sessionManager.setServerUrl(AppConstants.DEMO_URL);
                Intent intent = new Intent(SplashScreenActivity.this, HomeScreenActivity_New.class);
                startActivity(intent); // TODO: remove this block code later.*/
            }
        });

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
                        MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(SplashScreenActivity.this);
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
        return true;
    }
/*
    private boolean checkAndRequestPermissions() {
        List<String> listPermissionsNeeded = new ArrayList<>();
        int cameraPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA);
        int getAccountPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS);
        int writeExternalStoragePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int phoneStatePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            writeExternalStoragePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES);
            int notificationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS);
            if (notificationPermission != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA);
        }
        if (getAccountPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.GET_ACCOUNTS);
        }
        if (writeExternalStoragePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
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
*/

    private void checkPerm() {
        if (checkAndRequestPermissions()) {
            if (sessionManager.isMigration()) {
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() { //Do something after 100ms
//                        nextActivity();
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
//                            nextActivity();
                        }
                    }
                }, 2000);
            }
        }
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
                if (sessionManager.isEnableAppLock())
                    fingerPrintAuthenticate();
                else
                    navigateToNextActivity();

            } else {
                Logger.logD(LOG_TAG, "Starting setup");
                Intent intent = new Intent(this, SetupPrivacyNoteActivity_New.class);
                startActivity(intent);
                finish();
            }

        }
    }

    private void animateViews() {
        emptyView.setVisibility(View.VISIBLE);
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Animation translateAnim = AnimationUtils.loadAnimation(getApplicationContext(),
                        R.anim.ui2_new_center_to_top);
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
            //jsonObject.put("name", "हिंदी");
            //jsonObject.put("code", "hi");

           // jsonObject.put("selected", sessionManager.getAppLanguage().isEmpty() || sessionManager.getAppLanguage().equalsIgnoreCase("hi"));
          //  itemList.add(jsonObject);

            jsonObject = new JSONObject();
            jsonObject.put("name", "English");
            jsonObject.put("code", "en");

            jsonObject.put("selected", sessionManager.getAppLanguage().isEmpty() || sessionManager.getAppLanguage().equalsIgnoreCase("en"));
            itemList.add(jsonObject);

            /*jsonObject = new JSONObject();
            jsonObject.put("name", "ଓଡିଆ");
            jsonObject.put("code", "or");

            jsonObject.put("selected", sessionManager.getAppLanguage().isEmpty() || sessionManager.getAppLanguage().equalsIgnoreCase("or"));
            itemList.add(jsonObject);

            jsonObject = new JSONObject();
            jsonObject.put("name", "తెలుగు");
            jsonObject.put("code", "te");

            jsonObject.put("selected", sessionManager.getAppLanguage().isEmpty() || sessionManager.getAppLanguage().equalsIgnoreCase("te"));
            itemList.add(jsonObject);

            jsonObject = new JSONObject();
            jsonObject.put("name", "ગુજરાતી");
            jsonObject.put("code", "gu");

            jsonObject.put("selected", sessionManager.getAppLanguage().isEmpty() || sessionManager.getAppLanguage().equalsIgnoreCase("gu"));
            itemList.add(jsonObject);

            jsonObject = new JSONObject();
            jsonObject.put("name", "मराठी");
            jsonObject.put("code", "mr");

            jsonObject.put("selected", sessionManager.getAppLanguage().isEmpty() || sessionManager.getAppLanguage().equalsIgnoreCase("mr"));
            itemList.add(jsonObject);

            jsonObject = new JSONObject();
            jsonObject.put("name", "ಕನ್ನಡ");
            jsonObject.put("code", "kn");

            jsonObject.put("selected", sessionManager.getAppLanguage().isEmpty() || sessionManager.getAppLanguage().equalsIgnoreCase("kn"));
            itemList.add(jsonObject);

            jsonObject = new JSONObject();
            jsonObject.put("name", "অসমীয়া");
            jsonObject.put("code", "as");

            jsonObject.put("selected", sessionManager.getAppLanguage().isEmpty() || sessionManager.getAppLanguage().equalsIgnoreCase("as"));
            itemList.add(jsonObject);

            jsonObject = new JSONObject();
            jsonObject.put("name", "മലയാളം");
            jsonObject.put("code", "ml");

            jsonObject.put("selected", sessionManager.getAppLanguage().isEmpty() || sessionManager.getAppLanguage().equalsIgnoreCase("ml"));
            itemList.add(jsonObject);

            jsonObject = new JSONObject();
            jsonObject.put("name", "русский");
            jsonObject.put("code", "ru");

            jsonObject.put("selected", sessionManager.getAppLanguage().equalsIgnoreCase("ru"));
            itemList.add(jsonObject);

            jsonObject = new JSONObject();
            jsonObject.put("name", "বাংলা");
            jsonObject.put("code", "bn");

            jsonObject.put("selected", sessionManager.getAppLanguage().isEmpty() || sessionManager.getAppLanguage().equalsIgnoreCase("bn"));
            itemList.add(jsonObject);

            jsonObject = new JSONObject();
            jsonObject.put("name", "தமிழ்");
            jsonObject.put("code", "ta");

            jsonObject.put("selected", sessionManager.getAppLanguage().isEmpty() || sessionManager.getAppLanguage().equalsIgnoreCase("ta"));
            itemList.add(jsonObject);*/

            ChooseLanguageAdapterNew languageListAdapter = new ChooseLanguageAdapterNew(SplashScreenActivity.this,
                    itemList, new ItemSelectionListener() {
                @Override
                public void onSelect(JSONObject jsonObject, int index) {
                    try {
                        sessionManager.setAppLanguage(jsonObject.getString("code"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
            rvSelectLanguage.setLayoutManager(layoutManager);
            rvSelectLanguage.setItemAnimator(new DefaultItemAnimator());
            rvSelectLanguage.setAdapter(languageListAdapter);


        } catch (JSONException e) {
            e.printStackTrace();
        }
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

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(getResources().getString(R.string.intelehealth_login))
                .setSubtitle(getResources().getString(R.string.touch_fingerprint))
                .setDeviceCredentialAllowed(true)
                .build();

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
                    showPermissionDeniedAlert(permissions);
                }
            }
        }
    }

    private void showPermissionDeniedAlert(String[] permissions) {
        DialogUtils dialogUtils = new DialogUtils();
        dialogUtils.showCommonDialog(this, 0, getString(R.string.permission_denied), getString(R.string.reject_permission_results), false, getString(R.string.retry_again),
                getString(R.string.ok_close_now), new DialogUtils.CustomDialogListener() {
                    @Override
                    public void onDialogActionDone(int action) {
                        if (action == DialogUtils.CustomDialogListener.POSITIVE_CLICK) {
                            checkPerm();

                        } else {
                            finish();
                        }
                    }
                });
    }

}