package org.intelehealth.app.activities.forgotPasswordNew;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.intelehealth.app.R;
import org.intelehealth.app.appointment.api.ApiClientAppointment;
import org.intelehealth.app.models.ForgotPasswordApiResponseModel;

import retrofit2.Call;
import retrofit2.Callback;


public class ForgotPasswordOtpVerificationActivity_New extends AppCompatActivity {
    private static final String TAG = "OtpVerificationActivity";
    //temporary OTP is hardcode
    String OTP = "111111";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification_ui2);

        Button buttonVerifyOtp = findViewById(R.id.button_verify_otp);
        LinearLayout layoutPinView = findViewById(R.id.pinview_otp);

        TextView tvPin1 = layoutPinView.findViewById(R.id.tv_pin_1);
        TextView tvPin2 = layoutPinView.findViewById(R.id.tv_pin_2);
        TextView tvPin3 = layoutPinView.findViewById(R.id.tv_pin_3);
        TextView tvPin4 = layoutPinView.findViewById(R.id.tv_pin_4);
        TextView tvPin5 = layoutPinView.findViewById(R.id.tv_pin_5);
        TextView tvPin6 = layoutPinView.findViewById(R.id.tv_pin_6);


        buttonVerifyOtp.setOnClickListener(v -> {
            String pin1 = tvPin1.getText().toString();
            String pin2 = tvPin2.getText().toString();
            String pin3 = tvPin3.getText().toString();
            String pin4 = tvPin4.getText().toString();
            String pin5 = tvPin5.getText().toString();
            String pin6 = tvPin5.getText().toString();


            if (!pin1.isEmpty() && !pin2.isEmpty() && !pin3.isEmpty() && !pin4.isEmpty() && !pin5.isEmpty() && !pin6.isEmpty()) {
                String otp = pin1 + pin2 + pin3 + pin4 + pin5 + pin6;
                Intent intent = new Intent(ForgotPasswordOtpVerificationActivity_New.this, ResetPasswordActivity_New.class);
                intent.putExtra("otp", otp);
                startActivity(intent);
            }
        });
    }


}