package org.intelehealth.app.activities.onboarding;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.LocaleList;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;

import org.intelehealth.app.R;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.utilities.FileUtils;
import org.intelehealth.app.utilities.SessionManager;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;
import java.util.Objects;

public class TermsAndConditionsActivity_New extends AppCompatActivity {
    private static final String TAG = "TermsAndConditionsActiv";
    private int mIntentFrom;
    JSONObject obj = null;
    TextView tvText;
    ImageView ivBack;
    String privacy_string;
    private Context context = TermsAndConditionsActivity_New.this;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_and_conditions_ui2);
      //  mIntentFrom = getIntent().getIntExtra("IntentFrom", 0);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.WHITE);
        }

        ivBack = findViewById(R.id.iv_back_arrow_terms);
        tvText = findViewById(R.id.tv_term_condition);
        sessionManager = new SessionManager(context);

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               finish();
            }
        });

      //  try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    // bg task
                    try {
                        obj = new JSONObject(Objects.requireNonNullElse(
                                FileUtils.readFileRoot(AppConstants.CONFIG_FILE_NAME, context),
                                String.valueOf(FileUtils.encodeJSON(context, AppConstants.CONFIG_FILE_NAME)))); //Load the config file
                        privacy_string = sessionManager.getAppLanguage().equalsIgnoreCase("hi") ?
                                obj.getString("terms_and_conditions_Hindi"):obj.getString("terms_and_conditions");
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // ui task
                            tvText.setText(HtmlCompat.fromHtml(privacy_string, HtmlCompat.FROM_HTML_MODE_LEGACY));
                        }
                    });
                }
            }).start();

//        } catch (JSONException e) {
//            e.printStackTrace();
//        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //overridePendingTransition(R.anim.ui2_slide_in_right, R.anim.ui2_slide_bottom_down);
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
}