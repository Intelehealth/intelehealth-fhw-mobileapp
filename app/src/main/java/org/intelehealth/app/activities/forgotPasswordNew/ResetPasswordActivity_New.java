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
import org.intelehealth.app.activities.setupActivity.SetupActivityNew;
import org.intelehealth.app.appointment.api.ApiClientAppointment;
import org.intelehealth.app.models.ChangePasswordParamsModel_New;
import org.intelehealth.app.models.ForgotPasswordApiResponseModel_New;
import org.intelehealth.app.models.ResetPasswordResModel_New;

import retrofit2.Call;
import retrofit2.Callback;

public class ResetPasswordActivity_New extends AppCompatActivity {
    private static final String TAG = "ResetPasswordActivity_N";
    String otp = "";
    String userUuid= "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password_ui2);


        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            otp = extras.getString("otp");
            userUuid = extras.getString("userUuid");
        }
        TextInputEditText etNewPassword = findViewById(R.id.et_new_password);
        TextInputEditText etConfirmPassword = findViewById(R.id.et_confirm_password);
        Button btnSavePassword = findViewById(R.id.btn_save_password);
        btnSavePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etNewPassword.getText().toString().equals(etConfirmPassword.getText().toString()) && !otp.isEmpty()) {
                    apiCallForResetPassword(ResetPasswordActivity_New.this, etNewPassword.getText().toString(), otp);
                }
            }
        });
    }

    private void apiCallForResetPassword(Context context, String newPassword, String otp) {
        //String baseurl = "https://" + new SessionManager(getActivity()).getServerUrl() + ":3004";
        String baseurl = "https://uiux.intelehealth.org:3004/";

        Log.d(TAG, "apiCallForResetPassword: newPassword : "+newPassword);
        Log.d(TAG, "apiCallForResetPassword: otp : "+otp);

        ChangePasswordParamsModel_New inputModel = new ChangePasswordParamsModel_New(newPassword,otp);

        ApiClientAppointment.getInstance(baseurl).getApi()
                .resetPassword(userUuid, inputModel)

                .enqueue(new Callback<ResetPasswordResModel_New>() {
                    @Override
                    public void onResponse(Call<ResetPasswordResModel_New> call, retrofit2.Response<ResetPasswordResModel_New> response) {
                        if (response.body() == null) return;
                        ResetPasswordResModel_New resetPasswordResModel_new = response.body();

                        if (resetPasswordResModel_new.getSuccess()) {
                            Toast.makeText(context, resetPasswordResModel_new.getMessage(), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(context, SetupActivityNew.class);
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onFailure(Call<ResetPasswordResModel_New> call, Throwable t) {
                        Log.v("onFailure", t.getMessage());
                    }
                });

    }

}