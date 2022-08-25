package org.intelehealth.app.ui2.onboarding;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import org.intelehealth.app.R;

public class PrivacyPolicyActivityNew extends AppCompatActivity {
    private static final String TAG = "PrivacyPolicyActivityNe";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy_new_ui2);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.ui2_slide_in_right, R.anim.ui2_slide_bottom_down);
    }

}