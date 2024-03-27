package org.intelehealth.app.activities.patientDetailActivity;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
import android.util.DisplayMetrics;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;

import org.intelehealth.app.R;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.shared.BaseActivity;
import org.intelehealth.app.utilities.ConfigUtils;
import org.intelehealth.app.utilities.SessionManager;

import java.util.Locale;

public class TeleconsultationConsentActivity extends BaseActivity {
    private static final String TAG = "TeleconsultationConsentActivity";

    SessionManager sessionManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teleconsultation_consent);
        sessionManager = new SessionManager(TeleconsultationConsentActivity.this);
        String consentText = new ConfigUtils(this).getTeleconsultationConsentText(sessionManager.getAppLanguage());
        WebView consentWebView = findViewById(R.id.content_tv);
        String text;
        text = "<html><body style='color:black;font-size: 0.8em;' >"; //style='text-align:justify;text-justify: inter-word;'
        text += consentText ;
        text += "</body></html>";
        consentWebView.loadData(text, "text/html", "utf-8");
        // changing status bar color
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.WHITE);
        }

        ImageView ivBack = findViewById(R.id.iv_back_arrow_terms);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //overridePendingTransition(R.anim.ui2_slide_in_right, R.anim.ui2_slide_bottom_down);
    }

    public void declineConsent(View view) {
        setResult(AppConstants.TELECONSULTATION_CONSENT_DECLINE);
        finish();
    }

    public void acceptConsent(View view) {
        setResult(AppConstants.TELECONSULTATION_CONSENT_ACCEPT);
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
}