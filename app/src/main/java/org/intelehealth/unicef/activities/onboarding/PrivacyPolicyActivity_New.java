package org.intelehealth.unicef.activities.onboarding;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.intelehealth.unicef.R;
import org.intelehealth.unicef.activities.base.LocalConfigActivity;
import org.intelehealth.unicef.activities.identificationActivity.IdentificationActivity_New;
import org.intelehealth.unicef.app.AppConstants;
import org.intelehealth.unicef.utilities.SessionManager;

public class PrivacyPolicyActivity_New extends LocalConfigActivity {
    private static final String TAG = "PrivacyPolicyActivityNe";
    private Button btn_accept_privacy;
    private int mIntentFrom;
    private SessionManager sessionManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(this);
        setLocale(sessionManager.getAppLanguage());
        setContentView(R.layout.activity_privacy_policy_new_ui2);

        Context context = this;
        // changing status bar color
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.WHITE);
        }
        mIntentFrom = getIntent().getIntExtra("IntentFrom", 0);
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

        btn_accept_privacy.setOnClickListener(v -> {
            if (mIntentFrom == AppConstants.INTENT_FROM_AYU_FOR_SETUP) {
                setResult(AppConstants.PRIVACY_POLICY_ACCEPT);
                finish();
            } else {
                Intent intent = new Intent(this, IdentificationActivity_New.class);
                startActivity(intent);
                finish();
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
}