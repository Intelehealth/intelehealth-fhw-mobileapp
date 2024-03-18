package org.intelehealth.app.activities.onboarding;

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
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResultRegistry;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.IntroActivity.IntroScreensActivity_New;
import org.intelehealth.app.activities.identificationActivity.IdentificationActivity_New;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.shared.BaseActivity;
import org.intelehealth.app.utilities.SessionManager;

import java.util.Locale;

public class PrivacyPolicyActivity_New extends BaseActivity {
    private static final String TAG = "PrivacyPolicyActivityNe";
    private Button btn_accept_privacy;
    private int mIntentFrom;
    String appLanguage, intentType;
    SessionManager sessionManager = null;

    ActivityResultLauncher<Intent> activityResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if(result.getResultCode() == AppConstants.PERSONAL_CONSENT_ACCEPT){
            Intent intent = new Intent(this, IdentificationActivity_New.class);
            startActivity(intent);
            finish();
        }else if(result.getResultCode() == AppConstants.PERSONAL_CONSENT_DECLINE){
            finish();
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy_new_ui2);
        sessionManager = new SessionManager(PrivacyPolicyActivity_New.this);

        // changing status bar color
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.WHITE);
        }
        mIntentFrom = getIntent().getIntExtra("IntentFrom", 0);
        intentType = getIntent().getStringExtra("intentType");
        ImageView ivBack = findViewById(R.id.iv_back_arrow_terms);
        btn_accept_privacy = findViewById(R.id.btn_accept_privacy);

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(PrivacyPolicyActivity_New.this, SetupPrivacyNoteActivity_New.class);
//                startActivity(intent); // TODO: add finish here...
                finish();
            }
        });

        //show button if it's from add patient
        if(!intentType.equalsIgnoreCase("doNotNavigateFurther")){
            findViewById(R.id.layout_button_privacy).setVisibility(View.VISIBLE);
        }else {
            findViewById(R.id.layout_button_privacy).setVisibility(View.GONE);
        }

        btn_accept_privacy.setOnClickListener(v -> {
            if(intentType.equalsIgnoreCase("doNotNavigateFurther")){
                setResult(AppConstants.PRIVACY_POLICY_ACCEPT);
                finish();
            }else {
                Intent intent = new Intent(this, PersonalConsentActivity.class);
                activityResult.launch(intent);
            }
        });

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //overridePendingTransition(R.anim.ui2_slide_in_right, R.anim.ui2_slide_bottom_down);
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
}