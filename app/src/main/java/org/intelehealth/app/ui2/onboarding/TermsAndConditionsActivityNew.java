package org.intelehealth.app.ui2.onboarding;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import org.intelehealth.app.R;

public class TermsAndConditionsActivityNew extends AppCompatActivity {
    private static final String TAG = "TermsAndConditionsActiv";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_and_conditions_ui2);
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.ui2_slide_in_right, R.anim.ui2_slide_bottom_down);
    }

}