package org.intelehealth.app.abdm.activity;

import static org.intelehealth.app.abdm.utils.ABDMConstant.AADHAAR_CARD_SELECTION;
import static org.intelehealth.app.abdm.utils.ABDMConstant.ABHA_OTP_AADHAAR;
import static org.intelehealth.app.abdm.utils.ABDMConstant.ABHA_SELECTION;
import static org.intelehealth.app.abdm.utils.ABDMConstant.MOBILE_NUMBER_SELECTION;
import static org.intelehealth.app.abdm.utils.ABDMConstant.SCOPE_AADHAAR;
import static org.intelehealth.app.abdm.utils.ABDMConstant.SCOPE_ABHA_ADDRESS;
import static org.intelehealth.app.abdm.utils.ABDMConstant.SCOPE_ABHA_NUMBER;
import static org.intelehealth.app.abdm.utils.ABDMConstant.SCOPE_MOBILE;
import static org.intelehealth.app.utilities.DialogUtils.showOKDialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.LocaleList;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.github.ajalt.timberkt.Timber;

import org.intelehealth.app.R;
import org.intelehealth.app.abdm.dialog.AbhaOtpTypeDialogFragment;
import org.intelehealth.app.abdm.dialog.AccountSelectDialogFragment;
import org.intelehealth.app.abdm.model.AbhaProfileRequestBody;
import org.intelehealth.app.abdm.model.AbhaProfileResponse;
import org.intelehealth.app.abdm.model.ExistUserStatusResponse;
import org.intelehealth.app.abdm.model.MobileLoginApiBody;
import org.intelehealth.app.abdm.model.MobileLoginOnOTPVerifiedResponse;
import org.intelehealth.app.abdm.model.OTPResponse;
import org.intelehealth.app.abdm.model.OTPVerificationRequestBody;
import org.intelehealth.app.abdm.model.TokenResponse;
import org.intelehealth.app.abdm.utils.ABDMUtils;
import org.intelehealth.app.activities.identificationActivity.IdentificationActivity_New;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.databinding.ActivityAbhaCardVerificationBinding;
import org.intelehealth.app.utilities.DialogUtils;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.SnackbarUtils;
import org.intelehealth.app.utilities.StringUtils;
import org.intelehealth.app.utilities.UrlModifiers;
import org.intelehealth.app.utilities.VerhoeffAlgorithm;
import org.intelehealth.app.utilities.WindowsUtils;
import org.intelehealth.app.widget.materialprogressbar.CustomProgressDialog;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;


public class AbhaCardVerificationActivity extends AppCompatActivity {
    private final Context context = AbhaCardVerificationActivity.this;
    private static final String TAG = AbhaCardVerificationActivity.class.getSimpleName();
    private ActivityAbhaCardVerificationBinding binding;
    private String accessToken = "";
    private String abhaAuthType = ABHA_OTP_AADHAAR;
    private String optionSelected = AADHAAR_CARD_SELECTION;
    private static final String BEARER_AUTH = "Bearer ";
    private CustomProgressDialog cpd;
    private SnackbarUtils snackbarUtils;
    private SessionManager sessionManager = null;
    private CountDownTimer countDownTimer;
    private static int resendCounter = 2;

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAbhaCardVerificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setInitialization();

        binding.otpBox.setText("");

        setAadhaarCardVisibility();

        checkInternetConnection();

        setClickListener();

    }

    private void setInitialization() {
        WindowsUtils.setStatusBarColor(AbhaCardVerificationActivity.this);  // changing status bar color
        cpd = new CustomProgressDialog(context);
        snackbarUtils = new SnackbarUtils();
        sessionManager = new SessionManager(context);
    }

    private void checkInternetConnection() {
        if (!NetworkConnection.isOnline(context)) {    // no internet.
            showOKDialog(context, ContextCompat.getDrawable(context, R.drawable.ui2_ic_warning_internet),
                    getString(R.string.error_network), getString(R.string.you_need_an_active_internet_connection_to_use_this_feature),
                    getString(R.string.ok), action -> {
                        if (action == DialogUtils.CustomDialogListener.POSITIVE_CLICK) {
                            finish();
                        }
                    });
        }
    }

    private void resendCounterAttemptsTextDisplay() {
        if (resendCounter != 0)
            binding.tvResendCounter.setText(getResources().getString(R.string.number_of_retries_left, resendCounter));
        else {
            binding.tvResendCounter.setText(getString(R.string.maximum_number_of_retries_exceeded_please_try_again_after_10_mins));
            binding.resendBtn.setEnabled(false);
            binding.resendBtn.setTextColor(getColor(R.color.medium_gray));
            binding.resendBtn.setPaintFlags(binding.resendBtn.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        }
    }

    private void setClickListener() {

        binding.ivBackArrow.setOnClickListener(v -> finish());

        binding.layoutHaveABHANumber.buttonUsername.setOnClickListener(v -> setAadhaarCardVisibility());

        binding.layoutHaveABHANumber.buttonMobileNumber.setOnClickListener(v -> setMobileVisibility());

        binding.layoutHaveABHANumber.buttonAbhaNumber.setOnClickListener(v -> setAbhaCardVisibility());

        binding.resendBtn.setOnClickListener(v -> {
            if (resendCounter != 0) {
                resendCounter--;

                resendCounterAttemptsTextDisplay();
                resendOtp();
                binding.otpBox.setText("");
                callGenerateTokenApi();
            } else
                resendCounterAttemptsTextDisplay();
        });

        binding.sendOtpBtn.setOnClickListener(v -> {
            if (checkValidation()) {

                if (binding.sendOtpBtn.getTag() == null) {  // ie. fresh call - sending otp.
                    resendOtp();
                    callGenerateTokenApi();
                } else {
                    // ie. otp received and making call to enrollAadhaar api.
                    if (Objects.requireNonNull(binding.otpBox.getText()).toString().isEmpty()) {    // ie. OTP not entered in box.
                        snackbarUtils.showSnackLinearLayoutParentSuccess(context, binding.llActionBar,
                                StringUtils.getMessageTranslated(getString(R.string.please_enter_otp_received), sessionManager.getAppLanguage()), false);
                        return;
                    }

                    if (!binding.otpBox.getText().toString().isEmpty()) {

                        if (optionSelected.equalsIgnoreCase(MOBILE_NUMBER_SELECTION)) {   // via. mobile login
                            callOTPForMobileLoginVerificationApi((String) binding.sendOtpBtn.getTag(), binding.otpBox.getText().toString());
                        } else if (optionSelected.equalsIgnoreCase(ABHA_SELECTION)) {   // via. abha login
                            callOTPForABHALoginVerificationApi((String) binding.sendOtpBtn.getTag(), binding.otpBox.getText().toString());
                        } else {    // via. aadhar card
                            callOTPForAadhaarVerificationApi((String) binding.sendOtpBtn.getTag(), binding.otpBox.getText().toString());
                        }
                    }
                }
            }
        });

    }

    private void setOtpVisibility() {
        if (binding.flOtpBox.getVisibility() != View.VISIBLE) {
            binding.flOtpBox.setVisibility(View.VISIBLE);
            binding.rlResendOTP.setVisibility(View.VISIBLE);
            binding.llResendCounter.setVisibility(View.VISIBLE);
            resendCounterAttemptsTextDisplay();
            binding.resendBtn.setPaintFlags(binding.resendBtn.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        }
    }

    /**
     * This method is being used to set abha card layout visibility when user select abha card tab.
     */
    private void setAbhaCardVisibility() {
        optionSelected = ABHA_SELECTION;
        sessionManager.setAbhaLoginType(SessionManager.ABHA_LOGIN);
        binding.layoutHaveABHANumber.llAadharMobile.setVisibility(View.GONE);
        binding.layoutHaveABHANumber.flAbhaDetails.setVisibility(View.VISIBLE);
        binding.layoutHaveABHANumber.buttonAbhaNumber.setBackgroundResource(R.drawable.button_bg_forgot_pass_ui2);
        binding.layoutHaveABHANumber.buttonUsername.setBackgroundResource(R.drawable.button_bg_forgot_pass_disabled_ui2);
        binding.layoutHaveABHANumber.buttonMobileNumber.setBackgroundResource(R.drawable.button_bg_forgot_pass_disabled_ui2);
    }

    /**
     * This method is being used to set mobile number edit text layout visibility when user select mobile tab.
     */
    private void setMobileVisibility() {
        optionSelected = MOBILE_NUMBER_SELECTION;
        sessionManager.setAbhaLoginType(SessionManager.MOBILE_LOGIN);
        binding.layoutHaveABHANumber.llAadharMobile.setVisibility(View.VISIBLE);
        binding.layoutHaveABHANumber.layoutParentUsername.setVisibility(View.GONE);
        binding.layoutHaveABHANumber.layoutParentMobileNo.setVisibility(View.VISIBLE);
        binding.layoutHaveABHANumber.tvUsernameError.setVisibility(View.GONE);
        binding.layoutHaveABHANumber.flAbhaDetails.setVisibility(View.GONE);
        binding.layoutHaveABHANumber.buttonMobileNumber.setBackgroundResource(R.drawable.button_bg_forgot_pass_ui2);
        binding.layoutHaveABHANumber.buttonUsername.setBackgroundResource(R.drawable.button_bg_forgot_pass_disabled_ui2);
        binding.layoutHaveABHANumber.buttonAbhaNumber.setBackgroundResource(R.drawable.button_bg_forgot_pass_disabled_ui2);
        binding.layoutHaveABHANumber.edittextMobileNumber.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_input_fieldnew));
        binding.layoutHaveABHANumber.edittextUsername.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_input_fieldnew));
    }

    /**
     * This method is being used to set aadhaar card layout (Aadhaar input , Mobile number input) visibility when user select aadhaar card tab.
     */
    private void setAadhaarCardVisibility() {
        optionSelected = AADHAAR_CARD_SELECTION;
        sessionManager.setAbhaLoginType(SessionManager.AADHAAR_LOGIN);
        binding.layoutHaveABHANumber.llAadharMobile.setVisibility(View.VISIBLE);
        binding.layoutHaveABHANumber.layoutParentMobileNo.setVisibility(View.GONE);
        binding.layoutHaveABHANumber.layoutParentUsername.setVisibility(View.VISIBLE);
        binding.layoutHaveABHANumber.tvUsernameError.setVisibility(View.GONE);
        binding.layoutHaveABHANumber.flAbhaDetails.setVisibility(View.GONE);
        binding.layoutHaveABHANumber.buttonUsername.setBackgroundResource(R.drawable.button_bg_forgot_pass_ui2);
        binding.layoutHaveABHANumber.buttonMobileNumber.setBackgroundResource(R.drawable.button_bg_forgot_pass_disabled_ui2);
        binding.layoutHaveABHANumber.buttonAbhaNumber.setBackgroundResource(R.drawable.button_bg_forgot_pass_disabled_ui2);
        binding.layoutHaveABHANumber.edittextMobileNumber.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_input_fieldnew));
        binding.layoutHaveABHANumber.edittextUsername.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_input_fieldnew));
    }

    /**
     * This method is being used to generate token API , and this API will call before any type of verification.
     */
    private void callGenerateTokenApi() {   // Step 1.
        cpd.show(getString(R.string.otp_sending));
        binding.sendOtpBtn.setEnabled(false);    // btn disabled.
        binding.sendOtpBtn.setTag(null);    // resetting...

        Single<TokenResponse> tokenResponse = AppConstants.apiInterface.GET_TOKEN(UrlModifiers.getABDM_TokenUrl());
        new Thread(() -> {
            // api - start
            tokenResponse.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new DisposableSingleObserver<>() {
                        @Override
                        public void onSuccess(TokenResponse tokenResponse1) {
                            accessToken = BEARER_AUTH + tokenResponse1.getAccessToken();
                            if (optionSelected.equalsIgnoreCase(AADHAAR_CARD_SELECTION) || (optionSelected.equalsIgnoreCase(MOBILE_NUMBER_SELECTION))) {
                                sentOtpApi(accessToken, getSendOtpApiRequest());   // via. aadhaarEnroll api
                            } else if (optionSelected.equalsIgnoreCase(ABHA_SELECTION)) {
                                cpd.dismiss();
                                AbhaOtpTypeDialogFragment dialog = new AbhaOtpTypeDialogFragment();
                                dialog.openAuthSelectionDialogDialog(authType -> {
                                    abhaAuthType = authType;
                                    sentOtpApi(accessToken, getSendOtpApiRequest());
                                });
                                dialog.show(getSupportFragmentManager(), "");
                            }

                        }

                        @Override
                        public void onError(Throwable e) {
                            Toast.makeText(context, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                            Timber.tag(TAG).e("onError: callGenerateTokenApi: %s", e.toString());
                            binding.sendOtpBtn.setEnabled(true);
                            binding.sendOtpBtn.setText(R.string.send_otp);  // Send otp.
                            cancelResendAndHideView();
                            cpd.dismiss();
                        }
                    });
            // api - end
        }).start();

    }

    private MobileLoginApiBody getSendOtpApiRequest() {
        MobileLoginApiBody requestBody = new MobileLoginApiBody();
        switch (optionSelected) {
            case MOBILE_NUMBER_SELECTION -> {
                requestBody.setScope(SCOPE_MOBILE);
                requestBody.setValue(Objects.requireNonNull(binding.layoutHaveABHANumber.edittextMobileNumber.getText()).toString().trim());
            }
            case ABHA_SELECTION -> {
                String value = TextUtils.isEmpty(binding.layoutHaveABHANumber.abhaDetails.etAbhaNumber.getText()) ? Objects.requireNonNull(binding.layoutHaveABHANumber.abhaDetails.etAbhaAddress.getText()).toString().trim() : ABDMUtils.INSTANCE.formatIntoAbhaString(binding.layoutHaveABHANumber.abhaDetails.etAbhaNumber.getText().toString().trim());
                requestBody.setValue(Objects.requireNonNull(value).trim()); // mobile value.
                requestBody.setScope(TextUtils.isEmpty(binding.layoutHaveABHANumber.abhaDetails.etAbhaNumber.getText()) ? SCOPE_ABHA_ADDRESS : SCOPE_ABHA_NUMBER);
                requestBody.setAuthMethod(abhaAuthType);
            }
            default -> {
                requestBody.setValue(Objects.requireNonNull(binding.layoutHaveABHANumber.edittextUsername.getText()).toString().trim());
                requestBody.setScope(SCOPE_AADHAAR);
            }
        }

        return requestBody;
    }

    private void sentOtpApi(String accessToken, MobileLoginApiBody requestBody) {  // mobile: Step 2
        cpd.show(getString(R.string.otp_sending));

        String url = UrlModifiers.getMobileLoginVerificationUrl();
        // payload - end

        Single<Response<OTPResponse>> mobileResponseSingle = AppConstants.apiInterface.GET_OTP_FOR_MOBILE(url, accessToken, requestBody);
        new Thread(() -> {
            // api - start
            mobileResponseSingle
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new DisposableSingleObserver<>() {
                        @Override
                        public void onSuccess(Response<OTPResponse> otpResponse) {
                            cpd.dismiss();
                            if (otpResponse.code() == 200) {
                                setOtpVisibility();
                                snackbarUtils.showSnackLinearLayoutParentSuccess(context, binding.llActionBar,
                                        StringUtils.getMessageTranslated(otpResponse.body().getMessage(), sessionManager.getAppLanguage()), true);

                                Timber.tag(TAG).d("onSuccess: callMobileNumberVerificationApi: %s", otpResponse.toString());

                                assert otpResponse.body() != null;
                                binding.sendOtpBtn.setTag(otpResponse.body().getTxnId());
                                binding.sendOtpBtn.setText(getString(R.string.verify));
                                binding.sendOtpBtn.setEnabled(true);
                            } else if (otpResponse.code() == 404) {
                                switch (optionSelected) {
                                    case MOBILE_NUMBER_SELECTION ->
                                            Toast.makeText(context, R.string.the_mobile_number_you_have_entered_does_not_match_with_any_of_the_records_please_enter_a_different_number, Toast.LENGTH_SHORT).show();
                                    case ABHA_SELECTION ->
                                            Toast.makeText(context, R.string.please_enter_valid_abha, Toast.LENGTH_SHORT).show();
                                    default ->
                                            Toast.makeText(context, R.string.please_enter_valid_aadhaar, Toast.LENGTH_SHORT).show();
                                }
                                binding.sendOtpBtn.setEnabled(true);
                            } else {
                                if (otpResponse.errorBody() != null) {
                                    Toast.makeText(context, ABDMUtils.getErrorMessage1(otpResponse.errorBody()), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(context, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                                }
                                binding.sendOtpBtn.setEnabled(true);
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            binding.sendOtpBtn.setEnabled(true);
                            binding.sendOtpBtn.setText(R.string.send_otp);  // Send otp.
                            binding.otpBox.setText("");
                            Timber.tag(TAG).e("onError: callMobileNumberVerificationApi: %s", e.getMessage());
                            Toast.makeText(context, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                            cancelResendAndHideView();
                            cpd.dismiss();
                        }
                    });
            // api - end
        }).start();
    }

    private void callOTPForABHALoginVerificationApi(String txnId, String otp) {
        if (otp.length() < 6) {
            Toast.makeText(context, getString(R.string.please_enter_6_digit_valid_otp), Toast.LENGTH_SHORT).show();
            return;
        }
        // Mobile: Step 3
        cpd = new CustomProgressDialog(context);
        cpd.show(getString(R.string.verifying_otp));
        Timber.tag("callOTPForVerificationApi: ").d("parameters: " + txnId + ", " + otp);
        binding.sendOtpBtn.setEnabled(false);    // btn disabled.

        // payload
        String url = UrlModifiers.getOTPForMobileLoginVerificationUrl();
        OTPVerificationRequestBody requestBody = new OTPVerificationRequestBody();
        requestBody.setTxnId(txnId);
        requestBody.setOtp(otp);
        requestBody.setAuthMethod(abhaAuthType);
        requestBody.setScope(TextUtils.isEmpty(binding.layoutHaveABHANumber.abhaDetails.etAbhaNumber.getText()) ? SCOPE_ABHA_ADDRESS : SCOPE_ABHA_NUMBER);

        Single<Response<MobileLoginOnOTPVerifiedResponse>> mobileLoginOnOTPVerifiedResponseSingle =
                AppConstants.apiInterface.PUSH_OTP_FOR_MOBILE_LOGIN_VERIFICATION(url, accessToken, requestBody);
        new Thread(() -> mobileLoginOnOTPVerifiedResponseSingle
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSingleObserver<>() {
                    @Override
                    public void onSuccess(Response<MobileLoginOnOTPVerifiedResponse> response) {
                        cpd.dismiss();
                        handMobileOtpVerificationSuccess(response);
                    }

                    @Override
                    public void onError(Throwable e) {
                        cpd.dismiss();
                        binding.sendOtpBtn.setEnabled(true);
                        Toast.makeText(context, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                    }
                })).start();
    }

    private void handMobileOtpVerificationSuccess(Response<MobileLoginOnOTPVerifiedResponse> response) {
        if (response.code() == 200) {
            Timber.tag("callOTPForMobileLoginVerificationApi").d("onSuccess: %s", response.toString());
            String scope = TextUtils.isEmpty(binding.layoutHaveABHANumber.abhaDetails.etAbhaNumber.getText()) ? SCOPE_ABHA_ADDRESS : SCOPE_ABHA_NUMBER;
            if (response.body() != null) {
                MobileLoginOnOTPVerifiedResponse mobileLoginOnOTPVerifiedResponse = response.body();
                if (scope.equalsIgnoreCase(SCOPE_ABHA_ADDRESS)) {
                    if (!TextUtils.isEmpty(mobileLoginOnOTPVerifiedResponse.getToken())) {
                        String X_TOKEN = BEARER_AUTH + response.body().getToken();
                        callFetchUserProfileAPI(null, response.body().getTxnId(), X_TOKEN);
                    } else {
                        Toast.makeText(context, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                        binding.sendOtpBtn.setEnabled(true);
                    }
                } else {
                    if (mobileLoginOnOTPVerifiedResponse.getAccounts() != null && mobileLoginOnOTPVerifiedResponse.getAccounts().size() > 0) {
                        if (mobileLoginOnOTPVerifiedResponse.getAccounts().size() > 1) {
                            AccountSelectDialogFragment dialog = new AccountSelectDialogFragment();
                            dialog.openAccountSelectionDialog(mobileLoginOnOTPVerifiedResponse.getAccounts(), account -> {
                                String ABHA_NUMBER = account.getABHANumber();
                                String X_TOKEN = BEARER_AUTH + mobileLoginOnOTPVerifiedResponse.getToken();
                                callFetchUserProfileAPI(ABHA_NUMBER, mobileLoginOnOTPVerifiedResponse.getTxnId(), X_TOKEN);
                            });
                            dialog.show(getSupportFragmentManager(), "");
                        } else {
                            // ie. Only 1 account for this mobile number than call -> fetch User Profile details api.
                            String ABHA_NUMBER = mobileLoginOnOTPVerifiedResponse.getAccounts().get(0).getABHANumber();
                            String X_TOKEN = BEARER_AUTH + mobileLoginOnOTPVerifiedResponse.getToken();
                            callFetchUserProfileAPI(ABHA_NUMBER, mobileLoginOnOTPVerifiedResponse.getTxnId(), X_TOKEN);
                        }

                    } else {
                        Toast.makeText(context, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                        binding.sendOtpBtn.setEnabled(true);
                    }
                }

            } else {
                binding.sendOtpBtn.setEnabled(true);
                Timber.tag("callOTPForMobileLoginVerificationApi").d("onSuccess: %s", response.toString());
            }
        } else if (response.code() == 422) {
            binding.sendOtpBtn.setEnabled(true);
            Toast.makeText(context, getString(R.string.please_enter_valid_otp), Toast.LENGTH_SHORT).show();
        } else {
            binding.sendOtpBtn.setEnabled(true);
            Toast.makeText(context, getString(R.string.please_enter_valid_otp), Toast.LENGTH_SHORT).show();
            Timber.tag("callOTPForMobileLoginVerificationApi").d("onSuccess: %s", response.toString());
        }
    }

    /**
     * Here, this function will only be called if user has ABHA number and he wants to use the login via. Mobile login flow.
     *
     * @param txnId: txnId received in success response.
     * @param otp    : otp received via. SMS
     */
    private void callOTPForMobileLoginVerificationApi(String txnId, String otp) {   // Mobile: Step 3
        if (otp.length() < 6) {
            Toast.makeText(context, getString(R.string.please_enter_6_digit_valid_otp), Toast.LENGTH_SHORT).show();
            return;
        }
        cpd = new CustomProgressDialog(context);
        cpd.show(getString(R.string.verifying_otp));
        Timber.tag("callOTPForVerificationApi: ").d("parameters: " + txnId + ", " + otp);
        binding.sendOtpBtn.setEnabled(false);    // btn disabled.
        // resetting...

        // payload
        String url = UrlModifiers.getOTPForMobileLoginVerificationUrl();
        OTPVerificationRequestBody requestBody = new OTPVerificationRequestBody();
        requestBody.setTxnId(txnId);
        requestBody.setOtp(otp);
        requestBody.setScope(SCOPE_MOBILE);
        Single<Response<MobileLoginOnOTPVerifiedResponse>> mobileLoginOnOTPVerifiedResponseSingle =
                AppConstants.apiInterface.PUSH_OTP_FOR_MOBILE_LOGIN_VERIFICATION(url, accessToken, requestBody);
        new Thread(() -> mobileLoginOnOTPVerifiedResponseSingle
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSingleObserver<>() {
                    @Override
                    public void onSuccess(Response<MobileLoginOnOTPVerifiedResponse> response) {
                        cpd.dismiss();
                        if (response.code() == 200) {
                            if (response.body() != null) {
                                MobileLoginOnOTPVerifiedResponse mobileLoginOnOTPVerifiedResponse = response.body();
                                if (!mobileLoginOnOTPVerifiedResponse.getAuthResult().equalsIgnoreCase("failed")) {
                                    Timber.tag("callOTPForMobileLoginVerificationApi").d("onSuccess: %s", mobileLoginOnOTPVerifiedResponse.toString());
                                    if (mobileLoginOnOTPVerifiedResponse.getAccounts() != null && mobileLoginOnOTPVerifiedResponse.getAccounts().size() > 0) {
                                        binding.sendOtpBtn.setTag(null);
                                        if (mobileLoginOnOTPVerifiedResponse.getAccounts().size() > 1) {
                                            AccountSelectDialogFragment dialog = new AccountSelectDialogFragment();
                                            dialog.openAccountSelectionDialog(mobileLoginOnOTPVerifiedResponse.getAccounts(), account -> {
                                                String ABHA_NUMBER = account.getABHANumber();
                                                String X_TOKEN = BEARER_AUTH + mobileLoginOnOTPVerifiedResponse.getToken();
                                                callFetchUserProfileAPI(ABHA_NUMBER, mobileLoginOnOTPVerifiedResponse.getTxnId(), X_TOKEN);
                                            });
                                            dialog.show(getSupportFragmentManager(), "");
                                        } else {
                                            // ie. Only 1 account for this mobile number than call -> fetch User Profile details api.
                                            String ABHA_NUMBER = mobileLoginOnOTPVerifiedResponse.getAccounts().get(0).getABHANumber();
                                            String X_TOKEN = BEARER_AUTH + mobileLoginOnOTPVerifiedResponse.getToken();
                                            callFetchUserProfileAPI(ABHA_NUMBER, mobileLoginOnOTPVerifiedResponse.getTxnId(), X_TOKEN);
                                        }
                                    } else {
                                        Toast.makeText(context, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                                        binding.sendOtpBtn.setEnabled(true);
                                    }

                                } else {
                                    Toast.makeText(context, mobileLoginOnOTPVerifiedResponse.getMessage(), Toast.LENGTH_SHORT).show();
                                    binding.sendOtpBtn.setEnabled(true);
                                }
                            } else {
                                binding.sendOtpBtn.setEnabled(true);
                                Timber.tag("callOTPForMobileLoginVerificationApi").d("onSuccess: %s", response.toString());
                            }
                        } else {
                            Toast.makeText(context, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                            binding.sendOtpBtn.setEnabled(true);
                        }

                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.tag("callOTPForMobileLoginVerificationApi").e("onError: %s", e.toString());
                        handleOnOtpError();
                        Toast.makeText(context, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                    }
                })).start();
    }

    /**
     * Here, this function is used to call the EnrollByAadhaar api which takes @BODY: txtId, mobileNo, otp and will return us
     * patient's details.
     *
     * @param txnId get from aadhaar card verification api
     * @param otp   get from aadhaar card verification api
     */
    private void callOTPForAadhaarVerificationApi(String txnId, String otp) {
        if (otp.length() < 6) {
            Toast.makeText(context, getString(R.string.please_enter_6_digit_valid_otp), Toast.LENGTH_SHORT).show();
            return;
        }
        cpd = new CustomProgressDialog(context);
        cpd.show(getString(R.string.verifying_otp));
        Timber.tag("callOTPForVerificationApi: ").d("parameters: " + txnId + ", " + otp);
        binding.sendOtpBtn.setEnabled(false);    // btn disabled.

        // payload
        String url = UrlModifiers.getOTPForMobileLoginVerificationUrl();
        OTPVerificationRequestBody requestBody = new OTPVerificationRequestBody();
        requestBody.setTxnId(txnId);
        requestBody.setOtp(otp);
        requestBody.setScope(SCOPE_AADHAAR);

        Single<Response<MobileLoginOnOTPVerifiedResponse>> mobileLoginOnOTPVerifiedResponseSingle =
                AppConstants.apiInterface.PUSH_OTP_FOR_MOBILE_LOGIN_VERIFICATION(url, accessToken, requestBody);
        new Thread(() -> mobileLoginOnOTPVerifiedResponseSingle
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSingleObserver<>() {
                    @Override
                    public void onSuccess(Response<MobileLoginOnOTPVerifiedResponse> response) {
                        cpd.dismiss();

                        if (response.code() == 200) {
                            if (response.body() != null) {
                                MobileLoginOnOTPVerifiedResponse mobileLoginOnOTPVerifiedResponse = response.body();
                                Timber.tag("callOTPForMobileLoginVerificationApi").d("onSuccess: %s", mobileLoginOnOTPVerifiedResponse.toString());
                                if (mobileLoginOnOTPVerifiedResponse.getAccounts() != null && mobileLoginOnOTPVerifiedResponse.getAccounts().size() > 0) {
                                    String ABHA_NUMBER = mobileLoginOnOTPVerifiedResponse.getAccounts().get(0).getABHANumber();
                                    String X_TOKEN = BEARER_AUTH + mobileLoginOnOTPVerifiedResponse.getToken();
                                    callFetchUserProfileAPI(ABHA_NUMBER, mobileLoginOnOTPVerifiedResponse.getTxnId(), X_TOKEN);
                                    binding.sendOtpBtn.setTag(null);    // resetting...

                                } else {
                                    Toast.makeText(context, mobileLoginOnOTPVerifiedResponse.getMessage(), Toast.LENGTH_SHORT).show();
                                    binding.sendOtpBtn.setEnabled(true);
                                }
                            } else {
                                binding.sendOtpBtn.setEnabled(true);
                                Timber.tag("callOTPForMobileLoginVerificationApi").d("onSuccess: %s", response.toString());
                            }
                        } else {
                            Toast.makeText(context, ABDMUtils.getErrorMessage1(response.errorBody()), Toast.LENGTH_SHORT).show();
                            binding.sendOtpBtn.setEnabled(true);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.tag("callOTPForMobileLoginVerificationApi").e("onError: %s", e.toString());
                        binding.sendOtpBtn.setEnabled(true);
                        Toast.makeText(context, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                    }
                })).start();

    }

    /**
     * This will call the Fetch Profile Details api to fetch the details related to this user.
     */
    private void callFetchUserProfileAPI(String abhaNumber, String txnId, String xToken) {
        binding.sendOtpBtn.setEnabled(false);    // btn disabled.
        binding.sendOtpBtn.setTag(null);    // resetting...

        // payload - start
        String url = UrlModifiers.getABHAProfileUrl();
        AbhaProfileRequestBody requestBody = new AbhaProfileRequestBody();
        requestBody.setTxnId(txnId);
        requestBody.setAbhaNumber(abhaNumber);
        cpd.show();
        requestBody.setScope(optionSelected);
        if (!optionSelected.isEmpty() && optionSelected.equalsIgnoreCase(AADHAAR_CARD_SELECTION)) {
            requestBody.setScope(SCOPE_AADHAAR);
        } else if (!optionSelected.isEmpty() && optionSelected.equalsIgnoreCase(ABHA_SELECTION)) {
            requestBody.setScope(TextUtils.isEmpty(binding.layoutHaveABHANumber.abhaDetails.etAbhaNumber.getText()) ? SCOPE_ABHA_ADDRESS : SCOPE_ABHA_NUMBER);
        }
        // payload - end

        Single<AbhaProfileResponse> abhaProfileResponseSingle =
                AppConstants.apiInterface.PUSH_ABHA_PROFILE(url, accessToken, xToken, requestBody);
        new Thread(() -> abhaProfileResponseSingle
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSingleObserver<>() {
                    @Override
                    public void onSuccess(AbhaProfileResponse abhaProfileResponse) {
                        cpd.dismiss();
                        Timber.tag("callFetchUserProfileAPI").d("onSuccess: %s", abhaProfileResponse);
                        checkIsUserExist(abhaProfileResponse.getABHANumber(), abhaProfileResponse, xToken, requestBody);
                    }

                    @Override
                    public void onError(Throwable e) {
                        cpd.dismiss();
                        Timber.tag("callFetchUserProfileAPI").e("onError: %s", e.toString());
                    }
                })).start();

    }

    private void checkIsUserExist(String abhaAddress, AbhaProfileResponse abhaProfileResponse, String xToken, AbhaProfileRequestBody abhaProfileRequestBody) {

        sessionManager = new SessionManager(context);
        String encoded = sessionManager.getEncoded();
        String url = UrlModifiers.getCheckExistingUserUrl();
        cpd.show();
        // payload - end
        Single<ExistUserStatusResponse> abhaProfileResponseSingle =
                AppConstants.apiInterface.checkExistingUser(url + abhaAddress, "Basic " + encoded);
        new Thread(() -> abhaProfileResponseSingle
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSingleObserver<>() {
                    @Override
                    public void onSuccess(ExistUserStatusResponse response) {
                        cpd.dismiss();
                        Timber.tag("checkExistingUserAPI").d("onSuccess: %s", response);
                        Intent intent;
                        if (response != null && response.getData() != null &&
                                !Objects.requireNonNull(response.getData().getUuid()).equalsIgnoreCase("NA")) {
                            abhaProfileResponse.setOpenMrsId(response.getData().getOpenmrsid());
                            abhaProfileResponse.setUuiD(response.getData().getUuid());
                            intent = new Intent(context, IdentificationActivity_New.class);
                            intent.putExtra("mobile_payload", abhaProfileResponse);
                            intent.putExtra("accessToken", accessToken);
                            intent.putExtra("xToken", xToken);
                            intent.putExtra("txnId", abhaProfileRequestBody.getTxnId());
                            intent.putExtra("patient_detail", true);
                            startActivity(intent);
                        } else {
                            intent = new Intent(context, IdentificationActivity_New.class);
                            intent.putExtra("mobile_payload", abhaProfileResponse);
                            intent.putExtra("accessToken", accessToken);
                            intent.putExtra("xToken", xToken);
                            intent.putExtra("txnId", abhaProfileRequestBody.getTxnId());
                            startActivity(intent);
                        }
                        finish();
                    }

                    @Override
                    public void onError(Throwable e) {
                        cpd.dismiss();
                        Timber.tag("checkExistingUserAPI").e("onError: %s", e.toString());
                    }
                })).start();

    }


    private void handleOnOtpError() {
        cpd.dismiss();
        binding.sendOtpBtn.setEnabled(true);
        binding.sendOtpBtn.setText(R.string.send_otp);  // Send otp.
        binding.otpBox.setText("");
        cancelResendAndHideView();
    }

    public static boolean validateAadhaarNumber(String aadhaarNumber) {
        Pattern aadharPattern = Pattern.compile("\\d{12}");
        boolean isValidAadhaar = aadharPattern.matcher(aadhaarNumber).matches();
        if (isValidAadhaar) {
            isValidAadhaar = VerhoeffAlgorithm.validateVerhoeff(aadhaarNumber);
        }
        return isValidAadhaar;
    }

    private boolean checkValidation() {
        boolean isValid;

        isValid = areInputFieldsValid_HasABHA();

        // common area...
        if (binding.flOtpBox.getVisibility() == View.VISIBLE) {
            if (binding.otpBox.getText() != null) {
                if (binding.otpBox.getText().toString().isEmpty()) {
                    Toast.makeText(context, getString(R.string.please_enter_otp_received), Toast.LENGTH_LONG).show();
                    isValid = false;
                }
            }
        }

        return isValid;
    }

    private boolean areInputFieldsValid_HasABHA() {
        boolean isValid = true;
        if (!optionSelected.isEmpty() && optionSelected.equals(AADHAAR_CARD_SELECTION)) {

            // aadhaar validation - start
            String aadhaarNo = Objects.requireNonNull(binding.layoutHaveABHANumber.edittextUsername.getText()).toString().replace(" ", "").trim();
            if (aadhaarNo.isEmpty()) {
                binding.layoutHaveABHANumber.aadharError.setVisibility(View.VISIBLE);
                binding.layoutHaveABHANumber.aadharError.setText(getString(R.string.error_field_required));
                binding.layoutHaveABHANumber.edittextUsername.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));
                isValid = false;
            } else {
                if (aadhaarNo.length() != 12) {
                    binding.layoutHaveABHANumber.aadharError.setVisibility(View.VISIBLE);
                    binding.layoutHaveABHANumber.aadharError.setText(getString(R.string.enter_12_digits));
                    isValid = false;
                } else if (!validateAadhaarNumber(binding.layoutHaveABHANumber.edittextUsername.getText().toString())) {
                    binding.layoutHaveABHANumber.aadharError.setVisibility(View.VISIBLE);
                    binding.layoutHaveABHANumber.aadharError.setText(R.string.aadhar_number_is_not_valid);
                    binding.layoutHaveABHANumber.edittextUsername.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));
                    isValid = false;
                } else {
                    binding.layoutHaveABHANumber.edittextUsername.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_input_fieldnew));
                    binding.layoutHaveABHANumber.aadharError.setVisibility(View.GONE);
                }
            }   // aadhaar validation - end


            // mobile for aadhaar - end
        } else if (!optionSelected.isEmpty() && optionSelected.equals(MOBILE_NUMBER_SELECTION)) {  // Phone number field

            String mobile = Objects.requireNonNull(binding.layoutHaveABHANumber.edittextMobileNumber.getText()).toString().replace(" ", "").trim();
            Timber.tag(TAG).v(mobile);

            if (mobile.isEmpty()) {
                binding.layoutHaveABHANumber.mobileError.setVisibility(View.VISIBLE);
                binding.layoutHaveABHANumber.mobileError.setText(getString(R.string.error_field_required));
                binding.layoutHaveABHANumber.edittextMobileNumber.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));
                isValid = false;
            } else {
                if (/*code.equalsIgnoreCase("91") &&*/ mobile.length() != 10) {
                    binding.layoutHaveABHANumber.mobileError.setVisibility(View.VISIBLE);
                    binding.layoutHaveABHANumber.mobileError.setText(getString(R.string.enter_10_digits));
                    binding.layoutHaveABHANumber.edittextMobileNumber.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));
                    isValid = false;
                } else {
                    binding.layoutHaveABHANumber.edittextMobileNumber.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_input_fieldnew));
                    binding.layoutHaveABHANumber.mobileError.setVisibility(View.GONE);
                }
            }
        } else if (!optionSelected.isEmpty() && optionSelected.equals(ABHA_SELECTION)) {
            binding.layoutHaveABHANumber.abhaDetails.tvAbhaNumberError.setVisibility(View.GONE);
            binding.layoutHaveABHANumber.abhaDetails.tvAbhaNumberError.setVisibility(View.GONE);
            boolean isAbhaNumber = !TextUtils.isEmpty(binding.layoutHaveABHANumber.abhaDetails.etAbhaNumber.getText());
            if (TextUtils.isEmpty(binding.layoutHaveABHANumber.abhaDetails.etAbhaAddress.getText()) && TextUtils.isEmpty(binding.layoutHaveABHANumber.abhaDetails.etAbhaNumber.getText())) {
                binding.layoutHaveABHANumber.abhaDetails.tvAbhaNumberError.setVisibility(View.VISIBLE);
                binding.layoutHaveABHANumber.abhaDetails.tvAbhaAddressError.setVisibility(View.VISIBLE);
                isValid = false;
            } else if (isAbhaNumber && binding.layoutHaveABHANumber.abhaDetails.etAbhaNumber.getText().length() < 14) {
                Toast.makeText(context, getText(R.string.please_enter_valid_abha), Toast.LENGTH_SHORT).show();
                isValid = false;
            }

        }

        return isValid;
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

    private void cancelResendAndHideView() {
        if (countDownTimer != null)
            countDownTimer.cancel();    // reset any existing countdown.

        if (binding.rlResendOTP.getVisibility() == View.VISIBLE) {   // hide resend view
            binding.rlResendOTP.setVisibility(View.GONE);
            binding.llResendCounter.setVisibility(View.GONE);
            if (resendCounter != 2)
                resendCounter++;
        }

        if (binding.flOtpBox.getVisibility() == View.VISIBLE) { // hide otp view
            binding.flOtpBox.setVisibility(View.GONE);
            if (binding.otpBox.getText() != null) {
                if (!binding.otpBox.getText().toString().isEmpty()) {
                    binding.otpBox.setText("");
                }
            }
        }
    }

    private void resendOtp() {
        binding.resendBtn.setEnabled(false);
        binding.resendBtn.setTextColor(getColor(R.color.medium_gray));
        binding.sendOtpBtn.setText(R.string.send_otp);  // Send otp.

        String resendTime = getResources().getString(R.string.resend_otp_in);

        if (countDownTimer != null)
            countDownTimer.cancel();    // reset any existing countdown.
        countDownTimer = new CountDownTimer(60000, 1000) {

            public void onTick(long millisUntilFinished) {
                if (resendCounter != 0) {
                    String time = resendTime + " " + millisUntilFinished / 1000 + " " + getResources().getString(R.string.seconds);
                    binding.resendBtn.setText(time);
                    Timber.tag(TAG).d("onTick: %s", time);
                }
            }

            public void onFinish() {
                if (resendCounter != 0) {
                    binding.resendBtn.setEnabled(true);
                    binding.resendBtn.setTextColor(getColor(R.color.colorPrimary));
                }

                binding.resendBtn.setText(getResources().getString(R.string.resend_otp));
                if (cpd != null && cpd.isShowing())
                    cpd.dismiss();
            }

        }.start();
    }


}