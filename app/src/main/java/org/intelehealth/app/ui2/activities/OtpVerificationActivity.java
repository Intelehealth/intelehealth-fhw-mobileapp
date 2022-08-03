package org.intelehealth.app.ui2.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import org.intelehealth.app.R;


public class OtpVerificationActivity extends AppCompatActivity {
    private static final String TAG = "OtpVerificationActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);
    }
}