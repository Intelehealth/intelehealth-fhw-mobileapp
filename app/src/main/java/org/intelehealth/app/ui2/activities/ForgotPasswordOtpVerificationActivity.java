package org.intelehealth.app.ui2.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import org.intelehealth.app.R;


public class ForgotPasswordOtpVerificationActivity extends AppCompatActivity {
    private static final String TAG = "OtpVerificationActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        Button buttonVerifyOtp = findViewById(R.id.button_verify_otp);
        buttonVerifyOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ForgotPasswordOtpVerificationActivity.this, ResetPasswordActivity.class);
                startActivity(intent);
            }
        });
    }
}