package org.intelehealth.app.activities.onboarding;

import android.annotation.SuppressLint;
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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.intelehealth.app.R;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.utilities.ConfigUtils;
import org.intelehealth.app.utilities.DialogUtils;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.WebViewStatus;
import org.json.JSONObject;

import java.util.Locale;

public class TermsAndConditionsActivity_New extends AppCompatActivity implements WebViewStatus {
    private static final String TAG = "TermsAndConditionsActiv";
    private int mIntentFrom;
    JSONObject obj = null;
    TextView tvText;
    ImageView ivBack;
    String terms_and_condition_string = "";
    private Context context = TermsAndConditionsActivity_New.this;
    private SessionManager sessionManager;
    private AlertDialog loadingDialog = null;
    WebView webView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_and_conditions_ui2);
        //  mIntentFrom = getIntent().getIntExtra("IntentFrom", 0);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        getWindow().setStatusBarColor(Color.WHITE);

        ivBack = findViewById(R.id.iv_back_arrow_terms);
        webView = findViewById(R.id.webview);
        sessionManager = new SessionManager(context);

        loadingDialog = new DialogUtils().showCommonLoadingDialog(this, getString(R.string.loading), getString(R.string.please_wait));
        ivBack.setOnClickListener(v -> finish());

        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new GenericWebViewClient(this));


            new Thread(() -> {
                // bg task
                runOnUiThread(() -> {
                    // ui task
                    String text;
                    text = "<html><body style='color:black;font-size: 0.8em;' >"; //style='text-align:justify;text-justify: inter-word;'
                    text += new ConfigUtils(this).getTermsAndConditionsText(sessionManager.getAppLanguage());
                    text += "</body></html>";
                    webView.loadDataWithBaseURL(null,text, "text/html", "utf-8",null);
                });
            }).start();

    }


    public void declineTC(View view) {
        setResult(AppConstants.TERMS_CONDITIONS_DECLINE);
        finish();
    }

    public void acceptPP(View view) {
        setResult(AppConstants.TERMS_CONDITIONS_ACCEPT);
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

    }
}