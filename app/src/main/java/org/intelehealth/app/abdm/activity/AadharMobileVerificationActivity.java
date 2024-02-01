package org.intelehealth.app.abdm.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;

import org.intelehealth.app.R;
import org.intelehealth.app.utilities.WindowsUtils;

public class AadharMobileVerificationActivity extends AppCompatActivity {
    private Context context = AadharMobileVerificationActivity.this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aadhar_mobile_verification);

        // changing status bar color
        WindowsUtils.setStatusBarColor(AadharMobileVerificationActivity.this);
    }
}