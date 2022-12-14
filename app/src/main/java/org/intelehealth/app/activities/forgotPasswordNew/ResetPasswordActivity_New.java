package org.intelehealth.app.activities.forgotPasswordNew;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import org.intelehealth.app.R;
import org.intelehealth.app.appointment.api.ApiClientAppointment;
import org.intelehealth.app.models.ForgotPasswordApiResponseModel;

import retrofit2.Call;
import retrofit2.Callback;

public class ResetPasswordActivity_New extends AppCompatActivity {
    private static final String TAG = "ResetPasswordActivity_N";
    String otp = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password_ui2);


        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            otp = extras.getString("otp");
        }
        TextInputEditText etNewPassword = findViewById(R.id.et_new_password);
        TextInputEditText etConfirmPassword = findViewById(R.id.et_confirm_password);
        Button btnSavePassword = findViewById(R.id.btn_save_password);
        btnSavePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(etNewPassword.getText().toString().equals(etConfirmPassword.getText().toString()) && !otp.isEmpty()){
                    apiCallForResetPassword(ResetPasswordActivity_New.this, etNewPassword.getText().toString(), otp);
                }
            }
        });
    }

    private void apiCallForResetPassword(Context context, String newPassword, String otp) {
        //String baseurl = "https://" + new SessionManager(getActivity()).getServerUrl() + ":3004";
        String baseurl = "https://" + "https://uiux.intelehealth.org:3005";

        ApiClientAppointment.getInstance(baseurl).getApi()
                .resetPassword("",
                        "")

                .enqueue(new Callback<ForgotPasswordApiResponseModel>() {
                    @Override
                    public void onResponse(Call<ForgotPasswordApiResponseModel> call, retrofit2.Response<ForgotPasswordApiResponseModel> response) {
                        if (response.body() == null) return;
                        ForgotPasswordApiResponseModel forgotPasswordApiResponseModel = response.body();

                        if (forgotPasswordApiResponseModel.getSuccess()) {
                            Toast.makeText(context, forgotPasswordApiResponseModel.getMessage(), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(context, ForgotPasswordOtpVerificationActivity_New.class);
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onFailure(Call<ForgotPasswordApiResponseModel> call, Throwable t) {
                        Log.v("onFailure", t.getMessage());
                    }
                });

    }

}