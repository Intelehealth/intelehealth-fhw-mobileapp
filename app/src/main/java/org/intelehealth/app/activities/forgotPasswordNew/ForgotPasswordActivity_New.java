package org.intelehealth.app.activities.forgotPasswordNew;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.LocaleList;
import android.os.StrictMode;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import org.intelehealth.app.utilities.CustomLog;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.hbb20.CountryCodePicker;

import org.intelehealth.app.BuildConfig;
import org.intelehealth.app.R;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.models.ForgotPasswordApiResponseModel_New;
import org.intelehealth.app.models.RequestOTPParamsModel_New;
import org.intelehealth.app.networkApiCalls.ApiClient;
import org.intelehealth.app.networkApiCalls.ApiInterface;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.SnackbarUtils;
import org.intelehealth.app.utilities.StringUtils;
import org.intelehealth.app.widget.materialprogressbar.CustomProgressDialog;

import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ForgotPasswordActivity_New extends AppCompatActivity {
    private static final String TAG = ForgotPasswordActivity_New.class.getSimpleName();
    private TextInputEditText mUsernameEditText, mMobileNoEditText;
    CustomProgressDialog cpd;
    SessionManager sessionManager = null;
    Context context;
    LinearLayout layoutParent;
    SnackbarUtils snackbarUtils;
    ImageView imageviewBack;
    TextView tvUsernameError, tvMobileError;
    String mOptionSelected = "username";
    private CountryCodePicker countryCodePicker;
    private Button mButtonContinue;
    private int mActionType = 0;
    private Button mButtonUsername;

    private Button mButtonMobileNumber;
    private RelativeLayout mLayoutMobileNo;
    private LinearLayout mLayoutUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password_new_ui2);
        snackbarUtils = new SnackbarUtils();
        cpd = new CustomProgressDialog(ForgotPasswordActivity_New.this);
        sessionManager = new SessionManager(ForgotPasswordActivity_New.this);
        context = ForgotPasswordActivity_New.this;
        initUI();
        clickListeners();
        manageErrorFields();
        if (getIntent().hasExtra("action"))
            mActionType = getIntent().getIntExtra("action", 0);
    }

    private void setUIForForgotUserName() {
        // change header test
        ((TextView) findViewById(R.id.tv_forgot_password)).setText(getString(R.string.forgot_username_txt));

        // show enter phone number & short desc
        findViewById(R.id.ll_user_name_extra_label).setVisibility(View.VISIBLE);

        // hide action tabs
        mButtonUsername.setVisibility(View.GONE);
        mButtonMobileNumber.setVisibility(View.GONE);
        // show country code * mobile number filed
        switchToMobileTab();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mActionType == AppConstants.FORGOT_USER_NAME_ACTION) {
            setUIForForgotUserName();
        }
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

    private void clickListeners() {


        imageviewBack.setOnClickListener(v -> {
            /*Intent intent = new Intent(ForgotPasswordActivity_New.this, SetupActivityNew.class);
            startActivity(intent);
            finish();*/
            getOnBackPressedDispatcher().onBackPressed();
        });

        mButtonUsername.setOnClickListener(v -> {
            mOptionSelected = "username";
            mMobileNoEditText.setText("");
            mLayoutMobileNo.setVisibility(View.GONE);
            mLayoutUsername.setVisibility(View.VISIBLE);
            tvMobileError.setVisibility(View.GONE);
            tvUsernameError.setVisibility(View.GONE);
            mButtonUsername.setBackground(ContextCompat.getDrawable(context,R.drawable.button_bg_forgot_pass_ui2));
            mButtonMobileNumber.setBackground(ContextCompat.getDrawable(context,R.drawable.button_bg_forgot_pass_disabled_ui2));
            mMobileNoEditText.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_input_fieldnew));
            mUsernameEditText.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_input_fieldnew));

        });
        mButtonMobileNumber.setOnClickListener(v -> {
            switchToMobileTab();
        });

        mButtonContinue.setOnClickListener(v -> {
            if (areInputFieldsValid(mUsernameEditText.getText().toString().trim(), mMobileNoEditText.getText().toString().trim())) {
                apiCallForRequestOTP(ForgotPasswordActivity_New.this, mUsernameEditText.getText().toString().trim(),
                        mMobileNoEditText.getText().toString().trim());
            }
        });

    }

    private void switchToMobileTab() {
        mOptionSelected = "mobile";
        mUsernameEditText.setText("");
        tvMobileError.setVisibility(View.GONE);
        tvUsernameError.setVisibility(View.GONE);
        mLayoutUsername.setVisibility(View.GONE);
        mLayoutMobileNo.setVisibility(View.VISIBLE);
        mButtonMobileNumber.setBackground(ContextCompat.getDrawable(context,R.drawable.button_bg_forgot_pass_ui2));
        mButtonUsername.setBackground(ContextCompat.getDrawable(context,R.drawable.button_bg_forgot_pass_disabled_ui2));
        mMobileNoEditText.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_input_fieldnew));
        mUsernameEditText.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_input_fieldnew));
    }

    private void initUI() {
        mButtonUsername = findViewById(R.id.button_username);
        mButtonContinue = findViewById(R.id.button_continue);
        mButtonMobileNumber = findViewById(R.id.button_mobile_number);
        mLayoutMobileNo = findViewById(R.id.layout_parent_mobile_no);
        mLayoutUsername = findViewById(R.id.layout_parent_username);
        mUsernameEditText = findViewById(R.id.edittext_username);
        mMobileNoEditText = findViewById(R.id.edittext_mobile_number);
        layoutParent = findViewById(R.id.bottom_layout);
        imageviewBack = findViewById(R.id.imageview_back_forgot_password);
        tvUsernameError = findViewById(R.id.tv_username_error);
        tvMobileError = findViewById(R.id.tv_mobile_error);
        countryCodePicker = findViewById(R.id.countrycode_spinner_forgot);
        countryCodePicker.registerCarrierNumberEditText(mMobileNoEditText); // attaches the ccp spinner with the edittext
        countryCodePicker.setNumberAutoFormattingEnabled(false);

        mUsernameEditText.addTextChangedListener(new MyWatcher(mUsernameEditText));
        mMobileNoEditText.addTextChangedListener(new MyWatcher(mMobileNoEditText));
        setMobileNumberLimit();
    }

    private int mSelectedMobileNumberValidationLength = 0;
    private String mSelectedCountryCode = "";

    private void setMobileNumberLimit() {
        mSelectedCountryCode = countryCodePicker.getSelectedCountryCode();
        if (mSelectedCountryCode.equals("91")) {
            mSelectedMobileNumberValidationLength = 10;
        }
        mMobileNoEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        InputFilter inputFilter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                return null;
            }
        };

        mMobileNoEditText.setFilters(new InputFilter[]{inputFilter, new InputFilter.LengthFilter(mSelectedMobileNumberValidationLength)});
    }

    private class MyWatcher implements TextWatcher {
        EditText editText;

        MyWatcher(EditText editText) {
            this.editText = editText;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            String val = editable.toString().trim();
            if(editText.getId() == R.id.edittext_mobile_number) {
                mButtonContinue.setEnabled(val.length() == 10);
            }else{
                mButtonContinue.setEnabled(val.length() >= 5);
            }

        }
    }

    public void apiCallForRequestOTP(Context context, String username, String mobileNo) {
        cpd.show(getString(R.string.otp_sending));
        mButtonContinue.setEnabled(false);
        String serverUrl = BuildConfig.SERVER_URL + ":3004";
        CustomLog.d(TAG, "apiCallForRequestOTP: serverUrl : " + serverUrl);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        RequestOTPParamsModel_New inputModel = new RequestOTPParamsModel_New(mActionType == AppConstants.FORGOT_USER_NAME_ACTION ? "username" : "password", username, mobileNo, 91, "");
        CustomLog.d(TAG, "apiCallForRequestOTP: inputModel : " + new Gson().toJson(inputModel));
        ApiClient.changeApiBaseUrl(serverUrl);
        ApiInterface apiService = ApiClient.createService(ApiInterface.class);
        Observable<ForgotPasswordApiResponseModel_New> loginModelObservable = apiService.REQUEST_OTP_OBSERVABLE(inputModel);
        loginModelObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ForgotPasswordApiResponseModel_New>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(ForgotPasswordApiResponseModel_New forgotPasswordApiResponseModel_new) {
                        if (forgotPasswordApiResponseModel_new.getSuccess()) {
                            snackbarUtils.showSnackLinearLayoutParentSuccess(ForgotPasswordActivity_New.this, layoutParent, StringUtils.getMessageTranslated(forgotPasswordApiResponseModel_new.getMessage(), sessionManager.getAppLanguage()), true);
                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    //Toast.makeText(context, "Password changed successfully", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(ForgotPasswordActivity_New.this, ForgotPasswordOtpVerificationActivity_New.class);
                                    if (mActionType != AppConstants.FORGOT_USER_NAME_ACTION) {
                                        intent.putExtra("userUuid", forgotPasswordApiResponseModel_new.getData().getUuid());
                                    }
                                    intent.putExtra("userName", username);
                                    intent.putExtra("userPhoneNum", mobileNo);
                                    intent.putExtra("action", mActionType);
                                    startActivity(intent);
                                    finish();
                                }
                            }, 2000);
                        } else {
                            if (forgotPasswordApiResponseModel_new.getMessage().equalsIgnoreCase("Invalid username!")) {
                                mUsernameEditText.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));
                                tvUsernameError.setText(getResources().getString(R.string.username_not_found));
                                tvUsernameError.setVisibility(View.VISIBLE);
                            } else if (forgotPasswordApiResponseModel_new.getMessage().equalsIgnoreCase("Invalid phoneNumber!")) {
                                mMobileNoEditText.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));
                                tvMobileError.setText(getResources().getString(R.string.mobile_not_registered));
                                tvMobileError.setVisibility(View.VISIBLE);
                            } else {
                                Toast.makeText(ForgotPasswordActivity_New.this, StringUtils.getMessageTranslated(forgotPasswordApiResponseModel_new.getMessage(), sessionManager.getAppLanguage()), Toast.LENGTH_SHORT).show();
                            }
                        }
                        mButtonContinue.setEnabled(true);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.logD(TAG, "Login Failure" + e.getMessage());
                        e.printStackTrace();
                        cpd.dismiss();
                        snackbarUtils.showSnackLinearLayoutParentSuccess(context, layoutParent, getResources().getString(R.string.failed_to_send_otp), false);
                        mButtonContinue.setEnabled(true);

                    }

                    @Override
                    public void onComplete() {
                        cpd.dismiss();
                        Logger.logD(TAG, "completed");
                    }
                });

    }

    private void manageErrorFields() {

        mUsernameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    if (TextUtils.isEmpty(mUsernameEditText.getText().toString())) {
                        tvUsernameError.setVisibility(View.VISIBLE);
                        mUsernameEditText.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));
                        return;
                    } else {
                        tvUsernameError.setVisibility(View.GONE);
                        mUsernameEditText.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_input_fieldnew));
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mMobileNoEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(mMobileNoEditText.getText().toString())) {
                    tvMobileError.setVisibility(View.VISIBLE);
                    mMobileNoEditText.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));
                    return;
                } else {
                    tvMobileError.setVisibility(View.GONE);
                    mMobileNoEditText.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_input_fieldnew));
                }
            }
        });
    }

    private boolean areInputFieldsValid(String username, String mobile) {
        boolean result = false;
        if (!mOptionSelected.isEmpty() && mOptionSelected.equals("username")) {
            if (TextUtils.isEmpty(username)) {
                result = false;
                tvUsernameError.setVisibility(View.VISIBLE);
                mUsernameEditText.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));
            } else {
                result = true;
                mUsernameEditText.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_input_fieldnew));
            }
            return result;

        } else if (!mOptionSelected.isEmpty() && mOptionSelected.equals("mobile")) {
            String code = countryCodePicker.getSelectedCountryCode();
            mobile = mobile.replace(" ", "");
            CustomLog.v(TAG, code);
            CustomLog.v(TAG, mobile);
            if (TextUtils.isEmpty(mobile)) {
                result = false;
                tvMobileError.setVisibility(View.VISIBLE);
                mMobileNoEditText.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));

            } else if (code.equalsIgnoreCase("91") && mobile.trim().length() != 10) {
                result = false;
                tvMobileError.setVisibility(View.VISIBLE);
                tvMobileError.setText(getString(R.string.enter_10_digits));
                mMobileNoEditText.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));

            } else {
                mMobileNoEditText.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_input_fieldnew));
                result = true;
            }

            return result;
        }

        return result;
    }

}