package org.intelehealth.kf.activities.forgotPasswordNew;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.LocaleList;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.intelehealth.kf.R;
import org.intelehealth.kf.activities.setupActivity.SetupActivityNew;
import org.intelehealth.kf.app.AppConstants;
import org.intelehealth.kf.models.ForgotPasswordApiResponseModel_New;
import org.intelehealth.kf.models.OTPVerificationParamsModel_New;
import org.intelehealth.kf.models.RequestOTPParamsModel_New;
import org.intelehealth.kf.networkApiCalls.ApiClient;
import org.intelehealth.kf.networkApiCalls.ApiInterface;
import org.intelehealth.kf.utilities.Logger;
import org.intelehealth.kf.utilities.SessionManager;
import org.intelehealth.kf.utilities.SnackbarUtils;

import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;


public class ForgotPasswordOtpVerificationActivity_New extends AppCompatActivity {
    private static final String TAG = "OtpVerificationActivity";
    //temporary OTP is hardcode
    String OTP = "111111";
    String userUuid = "", userPhoneNum, userName;
    TextView tvOtpError, tvResendOtp;
    EditText etPin1, etPin2, etPin3, etPin4, etPin5, etPin6;
    LinearLayout layoutParent, rvHelpInfo;
    SnackbarUtils snackbarUtils;
    Button buttonVerifyOtp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification_ui2);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            userUuid = extras.getString("userUuid");
            userName = extras.getString("userName");
            userPhoneNum = extras.getString("userPhoneNum");
        }
        buttonVerifyOtp = findViewById(R.id.button_verify_otp);
        LinearLayout layoutPinView = findViewById(R.id.pinview_otp);

        etPin1 = layoutPinView.findViewById(R.id.et_pin_1);
        etPin2 = layoutPinView.findViewById(R.id.et_pin_2);
        etPin3 = layoutPinView.findViewById(R.id.et_pin_3);
        etPin4 = layoutPinView.findViewById(R.id.et_pin_4);
        etPin5 = layoutPinView.findViewById(R.id.et_pin_5);
        etPin6 = layoutPinView.findViewById(R.id.et_pin_6);
        tvOtpError = findViewById(R.id.tv_otp_error);
        tvResendOtp = findViewById(R.id.textview_no_otp);
        tvResendOtp.setPaintFlags(tvResendOtp.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        snackbarUtils = new SnackbarUtils();
        layoutParent = findViewById(R.id.layout_parent_otp);
        rvHelpInfo = findViewById(R.id.rv_help_info);
        etPin1.requestFocus();

        tvResendOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etPin1.setText("");
                etPin2.setText("");
                etPin3.setText("");
                etPin4.setText("");
                etPin5.setText("");
                etPin6.setText("");
                etPin1.requestFocus();
                if(tvOtpError.getVisibility()==View.VISIBLE)
                    tvOtpError.setVisibility(View.GONE);
                apiCallForRequestOTP(ForgotPasswordOtpVerificationActivity_New.this, userName, userPhoneNum);
            }
        });

        ImageView ivBack = findViewById(R.id.imageview_back_otp_verify);
        ivBack.setOnClickListener(v -> {
            Intent intent = new Intent(ForgotPasswordOtpVerificationActivity_New.this, SetupActivityNew.class);
            startActivity(intent);
        });

        rvHelpInfo.setOnClickListener(v -> {
            /*Intent intent = new Intent(ForgotPasswordOtpVerificationActivity_New.this, ChatSupportHelpActivity_New.class);
            startActivity(intent);*/

            //As socket implementation is pending thus adding this flow temporarily: JIRA Ticket IDA4-1130
            String phoneNumber = getString(R.string.support_mobile_no_1);
            String message = getString(R.string.help_whatsapp_string_2);
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse(
                            String.format("https://api.whatsapp.com/send?phone=%s&text=%s",
                                    phoneNumber, message))));
        });

        buttonVerifyOtp.setOnClickListener(v -> {
            String pin1 = etPin1.getText().toString();
            String pin2 = etPin2.getText().toString();
            String pin3 = etPin3.getText().toString();
            String pin4 = etPin4.getText().toString();
            String pin5 = etPin5.getText().toString();
            String pin6 = etPin6.getText().toString();

           /* Intent intent = new Intent(ForgotPasswordOtpVerificationActivity_New.this, ResetPasswordActivity_New.class);
            intent.putExtra("otp", "111111");
            intent.putExtra("userUuid", userUuid);
            startActivity(intent);*/
            if (!pin1.isEmpty() && !pin2.isEmpty() && !pin3.isEmpty() && !pin4.isEmpty() && !pin5.isEmpty() && !pin6.isEmpty()) {
                tvOtpError.setVisibility(View.GONE);
                String otp = pin1 + pin2 + pin3 + pin4 + pin5 + pin6;
                buttonVerifyOtp.setEnabled(false);
                verifyOTP(ForgotPasswordOtpVerificationActivity_New.this, otp);
            } else {
                tvOtpError.setVisibility(View.VISIBLE);
            }
        });

        handleEditextFocus();
        resendOtp();

    }

    private void verifyOTP(Context context, String otp) {
        String serverUrl = "https://" + AppConstants.DEMO_URL + ":3004";
        Log.d(TAG, "apiCallForRequestOTP: serverUrl : " + serverUrl);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        OTPVerificationParamsModel_New inputModel = new OTPVerificationParamsModel_New("password", userName, userPhoneNum, 91, "", otp);
        ApiClient.changeApiBaseUrl(serverUrl);
        ApiInterface apiService = ApiClient.createService(ApiInterface.class);
        Observable<ForgotPasswordApiResponseModel_New> loginModelObservable = apiService.VERFIY_OTP_OBSERVABLE(inputModel);
        loginModelObservable.subscribe(new Observer<ForgotPasswordApiResponseModel_New>() {
            @Override
            public void onSubscribe(Disposable d) {

            }
            @Override
            public void onNext(ForgotPasswordApiResponseModel_New forgotPasswordApiResponseModel_new) {
                if (forgotPasswordApiResponseModel_new.getSuccess()) {
                    snackbarUtils.showSnackLinearLayoutParentSuccess(ForgotPasswordOtpVerificationActivity_New.this, layoutParent, forgotPasswordApiResponseModel_new.getMessage());
                    Intent intent = new Intent(ForgotPasswordOtpVerificationActivity_New.this, ResetPasswordActivity_New.class);
                    intent.putExtra("otp", otp);
                    intent.putExtra("userUuid", userUuid);
                    startActivity(intent);
                    finish();
                } else {
                    tvOtpError.setVisibility(View.VISIBLE);
                    tvOtpError.setText(getResources().getString(R.string.otp_is_incorrect));
                    etPin6.requestFocus();
                    buttonVerifyOtp.setEnabled(true);
                }
            }
            @Override
            public void onError(Throwable e) {
                Logger.logD(TAG, "Login Failure" + e.getMessage());
                e.printStackTrace();
                snackbarUtils.showSnackLinearLayoutParentSuccess(context, layoutParent, getResources().getString(R.string.otp_verification_failed));
                etPin6.requestFocus();
                buttonVerifyOtp.setEnabled(true);

            }

            @Override
            public void onComplete() {
                Logger.logD(TAG, "completed");
            }
        });

    }

    public void apiCallForRequestOTP(Context context, String username, String mobileNo) {
        tvResendOtp.setEnabled(false);
        String serverUrl = "https://" + AppConstants.DEMO_URL + ":3004";
        Log.d(TAG, "apiCallForRequestOTP: serverUrl : " + serverUrl);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        RequestOTPParamsModel_New inputModel = new RequestOTPParamsModel_New("password", username, mobileNo, 91, "");
        ApiClient.changeApiBaseUrl(serverUrl);
        ApiInterface apiService = ApiClient.createService(ApiInterface.class);
        Observable<ForgotPasswordApiResponseModel_New> loginModelObservable = apiService.REQUEST_OTP_OBSERVABLE(inputModel);
        loginModelObservable.subscribe(new Observer<ForgotPasswordApiResponseModel_New>() {
            @Override
            public void onSubscribe(Disposable d) {

            }
            @Override
            public void onNext(ForgotPasswordApiResponseModel_New forgotPasswordApiResponseModel_new) {
                if (forgotPasswordApiResponseModel_new.getSuccess()) {
                    snackbarUtils.showSnackLinearLayoutParentSuccess(ForgotPasswordOtpVerificationActivity_New.this, layoutParent, forgotPasswordApiResponseModel_new.getMessage());
                    etPin1.requestFocus();
                } else {
                    snackbarUtils.showSnackLinearLayoutParentSuccess(context, layoutParent, getResources().getString(R.string.failed_to_send_otp));
                }
                tvResendOtp.setEnabled(true);
            }
            @Override
            public void onError(Throwable e) {
                Logger.logD(TAG, "Login Failure" + e.getMessage());
                e.printStackTrace();
                snackbarUtils.showSnackLinearLayoutParentSuccess(context, layoutParent, getResources().getString(R.string.failed_to_send_otp));
                tvResendOtp.setEnabled(true);
            }

            @Override
            public void onComplete() {
                Logger.logD(TAG, "completed");
            }
        });

    }


    private void handleEditextFocus() {

        //for focus to the next edittext
        etPin1.addTextChangedListener(new GenericTextWatcher(etPin1, etPin2));
        etPin2.addTextChangedListener(new GenericTextWatcher(etPin2, etPin3));
        etPin3.addTextChangedListener(new GenericTextWatcher(etPin3, etPin4));
        etPin4.addTextChangedListener(new GenericTextWatcher(etPin4, etPin5));
        etPin5.addTextChangedListener(new GenericTextWatcher(etPin5, etPin6));
        etPin6.addTextChangedListener(new GenericTextWatcher(etPin6, etPin6));

        //for focus to the previous edittext
        etPin1.setOnKeyListener(new GenericKeyEvent(etPin1, etPin1));
        etPin2.setOnKeyListener(new GenericKeyEvent(etPin2, etPin1));
        etPin3.setOnKeyListener(new GenericKeyEvent(etPin3, etPin2));
        etPin4.setOnKeyListener(new GenericKeyEvent(etPin4, etPin3));
        etPin5.setOnKeyListener(new GenericKeyEvent(etPin5, etPin4));
        etPin6.setOnKeyListener(new GenericKeyEvent(etPin6, etPin5));

    }

    class GenericKeyEvent implements View.OnKeyListener {
        EditText currentView;
        EditText previousView;

        public GenericKeyEvent(EditText currentView, EditText previousView) {
            this.currentView = currentView;
            this.previousView = previousView;
        }

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL &&
                    currentView.getText().toString().isEmpty()) {
                previousView.setText("");
                previousView.requestFocus();
                return true;
            }
            return false;
        }
    }

    public class GenericTextWatcher implements TextWatcher {
        private EditText etPrev;
        private EditText etNext;
        StringBuilder sb = new StringBuilder();

        public GenericTextWatcher(EditText etPrev, EditText etNext) {
            this.etPrev = etPrev;
            this.etNext = etNext;

        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (sb.length() == 0) {
                etPrev.requestFocus();
            }
        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            if (sb.length() == 1) {
                sb.deleteCharAt(0);
            }
        }

        @Override
        public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

            if (sb.length() == 0 & etPrev.length() == 1) {
                sb.append(arg0);
                etPrev.clearFocus();

                if (etNext != null) {
                    etNext.requestFocus();
                    etNext.setCursorVisible(true);
                }

            }

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

    private void resendOtp() {
        tvResendOtp.setEnabled(false);
        String resendTime = getResources().getString(R.string.resend_otp_in);
        new CountDownTimer(30000, 1000) {

            public void onTick(long millisUntilFinished) {
                String time = resendTime + " " + millisUntilFinished / 1000 + " " + getResources().getString(R.string.seconds);
                tvResendOtp.setText(time);
            }

            public void onFinish() {
                tvResendOtp.setEnabled(true);
                /*etPin1.setText("");
                etPin2.setText("");
                etPin3.setText("");
                etPin4.setText("");
                etPin5.setText("");
                etPin6.setText("");
                etPin1.requestFocus();*/
                tvResendOtp.setText(getResources().getString(R.string.resend_otp));

            }

        }.start();
    }


}