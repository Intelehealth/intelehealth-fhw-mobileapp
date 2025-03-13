package org.intelehealth.app.activities.onboarding;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
import android.util.DisplayMetrics;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import org.intelehealth.app.R;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.shared.BaseActivity;
import org.intelehealth.app.ui.patient.activity.PatientRegistrationActivity;
import org.intelehealth.app.utilities.ConfigUtils;
import org.intelehealth.app.utilities.DialogUtils;
import org.intelehealth.app.utilities.IntentKeys;
import org.intelehealth.app.utilities.PatientRegStage;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.WebViewStatus;

import java.util.Locale;

public class PrivacyPolicyActivity_New extends BaseActivity implements WebViewStatus {
    private static final String TAG = "PrivacyPolicyActivityNe";
    private Button btn_accept_privacy;
    private int mIntentFrom;
    String appLanguage, intentType;
    boolean isPersonalConsentRequired;
    WebView webView;
    SessionManager sessionManager = null;
    private AlertDialog loadingDialog;

    ActivityResultLauncher<Intent> activityResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == AppConstants.PERSONAL_CONSENT_ACCEPT ||
                result.getResultCode() == AppConstants.PERSONAL_CONSENT_DECLINE) {
            finish();
        }
    });

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy_new_ui2);
        sessionManager = new SessionManager(PrivacyPolicyActivity_New.this);

        // changing status bar color
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        getWindow().setStatusBarColor(Color.WHITE);
        mIntentFrom = getIntent().getIntExtra(IntentKeys.INTENT_FROM, 0);
        intentType = getIntent().getStringExtra(IntentKeys.INTENT_TYPE);
        isPersonalConsentRequired = getIntent().getBooleanExtra(IntentKeys.IS_PERSONAL_CONSENT_REQUIRED, false);
        ImageView ivBack = findViewById(R.id.iv_back_arrow_terms);
        btn_accept_privacy = findViewById(R.id.btn_accept_privacy);
        webView = findViewById(R.id.webview);

        loadingDialog = new DialogUtils().showCommonLoadingDialog(
                this,
                getString(R.string.loading),
                getString(R.string.please_wait)
        );

        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new GenericWebViewClient(this));
        String text;
        text = "<html><body style='color:black;font-size: 0.8em;' >"; //style='text-align:justify;text-justify: inter-word;'
        text += new ConfigUtils(this).getPrivacyPolicyText(sessionManager.getAppLanguage());
        text += "</body></html>";
        webView.loadDataWithBaseURL(null, text, "text/html", "utf-8", null);

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(PrivacyPolicyActivity_New.this, SetupPrivacyNoteActivity_New.class);
//                startActivity(intent); // TODO: add finish here...
                finish();
            }
        });

        //show button if it's from add patient
        if (!intentType.equalsIgnoreCase(IntentKeys.DO_NOT_NAVIGATE_FURTHER)) {
            findViewById(R.id.layout_button_privacy).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.layout_button_privacy).setVisibility(View.GONE);
        }

        btn_accept_privacy.setOnClickListener(v -> {
            if (intentType.equalsIgnoreCase(IntentKeys.DO_NOT_NAVIGATE_FURTHER)) {
                setResult(AppConstants.PRIVACY_POLICY_ACCEPT);
                finish();
            } else {
                if (isPersonalConsentRequired) {
                    Intent intent = new Intent(this, PersonalConsentActivity.class);
                    activityResult.launch(intent);
                } else {
                    PatientRegistrationActivity.startPatientRegistration(PrivacyPolicyActivity_New.this, null, PatientRegStage.PERSONAL);
                    finish();
                }

            }
        });

    }

    public void declinePP(View view) {
        setResult(AppConstants.PRIVACY_POLICY_DECLINE);
        finish();
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

    @Override
    public void onPageStarted() {
        loadingDialog.show();
    }

    @Override
    public void onPageFinish() {
        loadingDialog.dismiss();
    }

    @Override
    public void onPageError(@NonNull String error) {
        loadingDialog.dismiss();
    }
}