package org.intelehealth.app.activities.forgotPasswordNew;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import org.intelehealth.app.R;


public class ForgotPasswordOtpVerificationActivity_New extends AppCompatActivity {
    private static final String TAG = "OtpVerificationActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification_ui2);

        Button buttonVerifyOtp = findViewById(R.id.button_verify_otp);
        buttonVerifyOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ForgotPasswordOtpVerificationActivity_New.this, ResetPasswordActivity_New.class);
                startActivity(intent);
            }
        });
    }
}