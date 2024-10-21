package org.intelehealth.app.activities.forgotPasswordNew;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.LocaleList;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputEditText;

import org.intelehealth.app.BuildConfig;
import org.intelehealth.app.R;
import org.intelehealth.app.activities.setupActivity.SetupActivityNew;
import org.intelehealth.app.models.ChangePasswordParamsModel_New;
import org.intelehealth.app.models.ResetPasswordResModel_New;
import org.intelehealth.app.networkApiCalls.ApiClient;
import org.intelehealth.app.networkApiCalls.ApiInterface;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.SnackbarUtils;
import org.intelehealth.app.utilities.StringUtils;
import org.intelehealth.app.utilities.TooltipWindow;
import org.intelehealth.app.widget.materialprogressbar.CustomProgressDialog;

import java.security.SecureRandom;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ResetPasswordActivity_New extends AppCompatActivity {
    private static final String TAG = "ResetPasswordActivity_N";
    String otp = "";
    String userUuid = "";
    Context context;
    LinearLayout layoutParent, rvHelpInfo;
    CustomProgressDialog cpd;
    SnackbarUtils snackbarUtils;
    TextView tvErrorNewPassword, tvErrorConfirmPassword, tvGeneratePassword;
    TextInputEditText etNewPassword, etConfirmPassword;
    Button btnSavePassword;
    TooltipWindow tipWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password_ui2);

        handleBackPress();

        context = ResetPasswordActivity_New.this;
        cpd = new CustomProgressDialog(context);
        snackbarUtils = new SnackbarUtils();
        rvHelpInfo = findViewById(R.id.rv_help_info);
        layoutParent = findViewById(R.id.layout_parent_otp);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            otp = extras.getString("otp");
            userUuid = extras.getString("userUuid");
        }
        etNewPassword = findViewById(R.id.et_new_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnSavePassword = findViewById(R.id.btn_save_password);
        tipWindow = new TooltipWindow(ResetPasswordActivity_New.this);
        ImageView ivBack = findViewById(R.id.imageview_back_reset);

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getOnBackPressedDispatcher().onBackPressed();
            }
        });
        tvErrorNewPassword = findViewById(R.id.tv_error_new_password);
        tvErrorConfirmPassword = findViewById(R.id.tv_error_confirm_password);
        tvGeneratePassword = findViewById(R.id.tv_generate_password);
        tvGeneratePassword.setOnClickListener(v -> {
            randomString(8);
        });

        ImageView ivQuestionResetPassword = findViewById(R.id.ivQuestionResetPasswordScreen);
        ivQuestionResetPassword.setOnClickListener(v -> {
            if (!tipWindow.isTooltipShown())
                tipWindow.showToolTip(ivQuestionResetPassword, getResources().getString(R.string.generate_password_tooltip_text));
        });


        btnSavePassword.setOnClickListener(v -> {
            if (areInputFieldsValid()) {
                apiCallForResetPassword(ResetPasswordActivity_New.this, etNewPassword.getText().toString(), otp);

            }

        });

        rvHelpInfo.setOnClickListener(v -> {
            //As socket implementation is pending thus adding this flow temporarily: JIRA Ticket IDA4-1130
            String phoneNumber = getString(R.string.support_mobile_no_1);
            String message = getString(R.string.help_whatsapp_string_2);
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse(
                            String.format("https://api.whatsapp.com/send?phone=%s&text=%s",
                                    phoneNumber, message))));
        });

        manageErrorFields();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(setLocale(newBase));
    }

    public Context setLocale(Context context) {
        SessionManager sessionManager1 = new SessionManager(context);
        String appLanguage = sessionManager1.getAppLanguage();
        Resources res = context.getResources();
        Configuration conf = res.getConfiguration();
        Locale locale = new Locale(appLanguage);
        Locale.setDefault(locale);
        conf.setLocale(locale);
        context.createConfigurationContext(conf);
        DisplayMetrics dm = res.getDisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            conf.setLocales(new LocaleList(locale));
        } else {
            conf.locale = locale;
        }
        res.updateConfiguration(conf, dm);
        return context;
    }

    /**
     * removed onBackPressed function due to deprecation
     * and added this one to handle onBackPressed
     */
    private void handleBackPress() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
    }

    public void apiCallForResetPassword(Context context, String newPassword, String otp) {
        cpd.show(getString(R.string.please_wait));

        SessionManager sessionManager = new SessionManager(context);
        String serverUrl = BuildConfig.SERVER_URL + ":3004";
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        ///, "Basic YWRtaW46QWRtaW4xMjM="
        ChangePasswordParamsModel_New inputModel = new ChangePasswordParamsModel_New(newPassword/*, otp*/);

        ApiClient.changeApiBaseUrl(serverUrl);
        ApiInterface apiService = ApiClient.createService(ApiInterface.class);
        Observable<ResetPasswordResModel_New> loginModelObservable = apiService.RESET_PASSWORD_OBSERVABLE(userUuid,
                inputModel);
        loginModelObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResetPasswordResModel_New>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(ResetPasswordResModel_New resetPasswordResModel_new) {
                        cpd.dismiss();
                        snackbarUtils.showSnackLinearLayoutParentSuccess(ResetPasswordActivity_New.this, layoutParent, StringUtils.getMessageTranslated(resetPasswordResModel_new.getMessage(), sessionManager.getAppLanguage()), true);
                        if (resetPasswordResModel_new.getSuccess()) {
                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Intent intent = new Intent(context, SetupActivityNew.class);
                                    startActivity(intent);
                                    finish();
                                }
                            }, 2000);


                        }

                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.logD(TAG, "Login Failure" + e.getMessage());
                        e.printStackTrace();
                        cpd.dismiss();
                        snackbarUtils.showSnackLinearLayoutParentSuccess(context, layoutParent, getResources().getString(R.string.failed_to_change_password), false);

                    }

                    @Override
                    public void onComplete() {
                        Logger.logD(TAG, "completed");
                    }
                });

    }


    private void manageErrorFields() {
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

        etConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(etConfirmPassword.getText().toString())) {
                    tvErrorConfirmPassword.setVisibility(View.VISIBLE);
                    etConfirmPassword.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));

                    return;
                } else {
                    tvErrorConfirmPassword.setVisibility(View.GONE);
                    etConfirmPassword.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_input_fieldnew));

                }
            }
        });
    }

    private boolean areInputFieldsValid() {
        boolean result = false;
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(newPassword)) {
            tvErrorNewPassword.setVisibility(View.VISIBLE);
            etNewPassword.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));

        } else if (!StringUtils.isValidPassword(newPassword)) {
            tvErrorNewPassword.setText(getString(R.string.password_validation));
            tvErrorNewPassword.setVisibility(View.VISIBLE);
            etNewPassword.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));
        } else if (TextUtils.isEmpty(confirmPassword)) {
            tvErrorConfirmPassword.setVisibility(View.VISIBLE);
            etConfirmPassword.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));

        } else if (!newPassword.equals(confirmPassword)) {
            etConfirmPassword.setText("");
            tvErrorConfirmPassword.setText(getString(R.string.password_match));
            tvErrorConfirmPassword.setVisibility(View.VISIBLE);
            etConfirmPassword.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));

        } else {
            etNewPassword.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_input_fieldnew));
            etConfirmPassword.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_input_fieldnew));

            result = true;
        }

        return result;
    }

    /*public static boolean isValid(String passwordhere) {

        if (passwordhere.length() < 8) {
            return false;
        } else {

            for (int p = 0; p < passwordhere.length(); p++) {
                if (Character.isUpperCase(passwordhere.charAt(p))) {
                    break;
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
    }*/

    void randomString(int len) {
        String AB = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyz";
        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        }

        etNewPassword.setText(sb.toString());
        etConfirmPassword.setText(sb.toString());

    }


}