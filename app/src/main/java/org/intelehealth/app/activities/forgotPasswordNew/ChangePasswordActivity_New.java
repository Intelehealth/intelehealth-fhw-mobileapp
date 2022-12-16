package org.intelehealth.app.activities.forgotPasswordNew;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonObject;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.homeActivity.HomeScreenActivity_New;
import org.intelehealth.app.appointment.api.ApiClientAppointment;
import org.intelehealth.app.models.ChangePasswordModel_New;
import org.intelehealth.app.networkApiCalls.ApiClient;
import org.intelehealth.app.networkApiCalls.ApiInterface;
import org.intelehealth.app.ui2.utils.CheckInternetAvailability;
import org.intelehealth.app.utilities.Base64Utils;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.SnackbarUtils;
import org.intelehealth.app.utilities.UrlModifiers;
import org.intelehealth.app.widget.materialprogressbar.CustomProgressDialog;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;

public class ChangePasswordActivity_New extends AppCompatActivity {
    private static final String TAG = "ChangePasswordActivity_";
    TextInputEditText etCurrentPassword, etNewPassword, etNewPasswordConfirm;
    String encoded = null;
    CustomProgressDialog cpd;
    Context context;
    UrlModifiers urlModifiers = new UrlModifiers();
    Base64Utils base64Utils = new Base64Utils();
    SessionManager sessionManager = null;
    TextView tvErrorCurrentPassword, tvErrorNewPassword, tvErrorConfirmPassword;
    RelativeLayout layoutParent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password_new_ui2);


        View toolbar = findViewById(R.id.toolbar_common);
        TextView tvTitle = toolbar.findViewById(R.id.tv_screen_title_common);
        ImageView ivIsInternet = toolbar.findViewById(R.id.imageview_is_internet_common);

        context = ChangePasswordActivity_New.this;
        cpd = new CustomProgressDialog(context);
        sessionManager = new SessionManager(context);


        etCurrentPassword = findViewById(R.id.et_current_password);
        etNewPassword = findViewById(R.id.et_new_password_change);
        etNewPasswordConfirm = findViewById(R.id.et_new_password_confirm);
        layoutParent = findViewById(R.id.layout_parent);


        //error fields
        tvErrorCurrentPassword = findViewById(R.id.tv_error_current_password);
        tvErrorNewPassword = findViewById(R.id.tv_error_new_password);
        tvErrorConfirmPassword = findViewById(R.id.tv_error_confirm_password);

        Button btnSave = findViewById(R.id.btn_save_change);

        btnSave.setOnClickListener(v -> {
            SnackbarUtils snackbarUtils = new SnackbarUtils();
            snackbarUtils.hideKeyboard(ChangePasswordActivity_New.this);

            if (areInputFieldsValid())
                if (NetworkConnection.isOnline(this)) {
                    apiCallForChangePassword(etCurrentPassword.getText().toString(), etNewPassword.getText().toString());
                }
        });

        tvTitle.setText(getResources().getString(R.string.change_password));
        if (CheckInternetAvailability.isNetworkAvailable(this)) {
            ivIsInternet.setImageDrawable(getResources().getDrawable(R.drawable.ui2_ic_internet_available));
        } else {
            ivIsInternet.setImageDrawable(getResources().getDrawable(R.drawable.ui2_ic_no_internet));

        }

        manageErrorFields();
    }

    public void apiCallForChangePassword(String currentPassword, String newPassword) {
        cpd.show();
        String serverUrl = "https://uiux.intelehealth.org/";
        // String urlString = urlModifiers.loginUrl(sessionManager.getServerUrl());

        //cpd.show();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Log.d(TAG, "UserLoginTask: encoded : " + sessionManager.getEncoded());

        ChangePasswordModel_New inputModel = new ChangePasswordModel_New(currentPassword, newPassword);

        ApiClient.changeApiBaseUrl(serverUrl);
        ApiInterface apiService = ApiClient.createService(ApiInterface.class);
        Observable<ResponseBody> loginModelObservable = apiService.CHANGE_PASSWORD_OBSERVABLE(inputModel,
                "Basic " + sessionManager.getEncoded());
        loginModelObservable.subscribe(new Observer<ResponseBody>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(ResponseBody test) {
                SnackbarUtils snackbarUtils = new SnackbarUtils();
                snackbarUtils.showSnacksWithRelativeLayoutSuccess(context, context.getString(R.string.password_changed_successfully),
                        layoutParent);

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //Toast.makeText(context, "Password changed successfully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(context, HomeScreenActivity_New.class);
                        startActivity(intent);
                    }
                }, 2000);

                cpd.dismiss();
            }

            @Override
            public void onError(Throwable e) {
                Logger.logD(TAG, "Login Failure" + e.getMessage());
                e.printStackTrace();
                cpd.dismiss();

                // snackbarUtils.showSnackCoordinatorLayoutParentSuccess(LoginActivityNew.this, layoutParent, getResources().getString(R.string.profile_details_updated_new));

                Toast.makeText(context, "" + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                // mEmailSignInButton.setText(getString(R.string.action_sign_in));
                //mEmailSignInButton.setEnabled(true);
            }

            @Override
            public void onComplete() {
                Logger.logD(TAG, "completed");
            }
        });

    }

    private boolean areInputFieldsValid() {
        boolean result = false;
        String currentPassword = etCurrentPassword.getText().toString();
        String newPassword = etNewPassword.getText().toString();
        String confirmPassword = etNewPasswordConfirm.getText().toString();

        if (TextUtils.isEmpty(currentPassword)) {
            result = false;
            tvErrorCurrentPassword.setVisibility(View.VISIBLE);
            etCurrentPassword.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));


        } else if (TextUtils.isEmpty(newPassword)) {
            result = false;
            tvErrorNewPassword.setVisibility(View.VISIBLE);
            etNewPassword.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));

        } else if (TextUtils.isEmpty(confirmPassword)) {
            result = false;
            tvErrorConfirmPassword.setVisibility(View.VISIBLE);
            etNewPasswordConfirm.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));

        } else if (newPassword.length() < 8) {
            if (!isValid(etNewPassword.getText().toString())) {
                tvErrorNewPassword.setText(getString(R.string.password_validation));
                tvErrorNewPassword.setVisibility(View.VISIBLE);
                etNewPassword.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));
            }
        } else if (!newPassword.equals(confirmPassword)) {
            result = false;
            etNewPasswordConfirm.setText("");
            tvErrorConfirmPassword.setText(getString(R.string.password_match));
            tvErrorConfirmPassword.setVisibility(View.VISIBLE);
            etNewPasswordConfirm.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));

        } else {
            etCurrentPassword.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_input_fieldnew));
            etNewPassword.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_input_fieldnew));
            etNewPasswordConfirm.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_input_fieldnew));

            result = true;
        }

        return result;
    }

    private void manageErrorFields() {

        etCurrentPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    if (TextUtils.isEmpty(etCurrentPassword.getText().toString())) {
                        tvErrorCurrentPassword.setVisibility(View.VISIBLE);
                        etCurrentPassword.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));

                        return;
                    } else {
                        tvErrorCurrentPassword.setVisibility(View.GONE);
                        etCurrentPassword.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_input_fieldnew));

                    }
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        etNewPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(etNewPassword.getText().toString())) {
                    tvErrorNewPassword.setVisibility(View.VISIBLE);
                    etNewPassword.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));

                    return;
                } else {
                    tvErrorNewPassword.setVisibility(View.GONE);
                    etNewPassword.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_input_fieldnew));

                }
            }
        });

        etNewPasswordConfirm.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(etNewPasswordConfirm.getText().toString())) {
                    tvErrorConfirmPassword.setVisibility(View.VISIBLE);
                    etNewPasswordConfirm.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));

                    return;
                } else {
                    tvErrorConfirmPassword.setVisibility(View.GONE);
                    etNewPasswordConfirm.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_input_fieldnew));

                }
            }
        });
    }

    public static boolean isValid(String passwordhere) {

        if (passwordhere.length() < 8) {
            return false;
        } else {

            for (int p = 0; p < passwordhere.length(); p++) {
                if (Character.isUpperCase(passwordhere.charAt(p))) {
                }
            }
            for (int q = 0; q < passwordhere.length(); q++) {
                if (Character.isLowerCase(passwordhere.charAt(q))) {
                }
            }
            for (int r = 0; r < passwordhere.length(); r++) {
                if (Character.isDigit(passwordhere.charAt(r))) {
                }
            }
            return true;
        }
    }


}