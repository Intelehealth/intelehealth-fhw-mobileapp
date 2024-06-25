package org.intelehealth.app.abdm.activity;

import static org.intelehealth.app.activities.identificationActivity.IdentificationActivity_New.PAYLOAD;
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

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.github.ajalt.timberkt.Timber;

import org.intelehealth.app.R;
import org.intelehealth.app.abdm.MobileNumberOtpVerificationDialog;
import org.intelehealth.app.abdm.model.AadharApiBody;
import org.intelehealth.app.abdm.model.EnrollSuggestionRequestBody;
import org.intelehealth.app.abdm.model.EnrollSuggestionResponse;
import org.intelehealth.app.abdm.model.ExistUserStatusResponse;
import org.intelehealth.app.abdm.model.OTPResponse;
import org.intelehealth.app.abdm.model.OTPVerificationRequestBody;
import org.intelehealth.app.abdm.model.OTPVerificationResponse;
import org.intelehealth.app.abdm.model.TokenResponse;
import org.intelehealth.app.abdm.utils.ABDMConstant;
import org.intelehealth.app.activities.identificationActivity.IdentificationActivity_New;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.databinding.ActivityCreateAbhaBinding;
import org.intelehealth.app.utilities.DialogUtils;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.SnackbarUtils;
import org.intelehealth.app.utilities.StringUtils;
import org.intelehealth.app.utilities.UrlModifiers;
import org.intelehealth.app.utilities.VerhoeffAlgorithm;
import org.intelehealth.app.utilities.WindowsUtils;
import org.intelehealth.app.widget.materialprogressbar.CustomProgressDialog;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;


public class CreateAbhaActivity extends AppCompatActivity {

    private final Context context = CreateAbhaActivity.this;

    public static final String TAG = AadharMobileVerificationActivity.class.getSimpleName();
    ActivityCreateAbhaBinding binding;
    private String accessToken = "";


    public static final String BEARER_AUTH = "Bearer ";
    private CustomProgressDialog cpd;
    SnackbarUtils snackbarUtils;
    SessionManager sessionManager = null;
    private CountDownTimer countDownTimer;
    private static int resendCounter = 2;


    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding =  ActivityCreateAbhaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        WindowsUtils.setStatusBarColor(CreateAbhaActivity.this);  // changing status bar color
        cpd = new CustomProgressDialog(context);
        snackbarUtils = new SnackbarUtils();
        sessionManager = new SessionManager(context);

        binding.ivBackArrow.setOnClickListener(v -> finish());

        // check internet - start
        checkInternetConnection();
        setClickListener();

    }

    private void setClickListener() {
        binding.resendBtn.setOnClickListener(v -> {
            if (resendCounter != 0) {
                resendCounter--;

                resendCounterAttemptsTextDisplay();
                resendOtp();
                binding.otpBox.setText("");
                callGenerateTokenApi();
            }
            else
                resendCounterAttemptsTextDisplay();
        });

        binding.sendOtpBtn.setOnClickListener(v -> {
            if (checkValidation()) {
                if (binding.flOtpBox.getVisibility() != View.VISIBLE) {
                    binding.flOtpBox.setVisibility(View.VISIBLE);
                    binding.rlResendOTP.setVisibility(View.VISIBLE);
                    binding.llResendCounter.setVisibility(View.VISIBLE);
                    resendCounterAttemptsTextDisplay();
                    binding.resendBtn.setPaintFlags(binding.resendBtn.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                }

                if (binding.sendOtpBtn.getTag() == null) {  // ie. fresh call - sending otp.
                    resendOtp();
                    callGenerateTokenApi();
                } else {
                    // ie. otp received and making call to enrollAadhaar api.
                    if (Objects.requireNonNull(binding.otpBox.getText()).toString().isEmpty()) {    // ie. OTP not entered in box.
                        snackbarUtils.showSnackLinearLayoutParentSuccess(context, binding.layoutParent,
                                StringUtils.getMessageTranslated(getString(R.string.please_enter_otp_received), sessionManager.getAppLanguage()), false);
                        return;
                    }

                    if (!binding.otpBox.getText().toString().isEmpty()) {
                        String mobileNo;
                        mobileNo = Objects.requireNonNull(binding.layoutDoNotHaveABHANumber.mobileNoBox.getText()).toString().trim();
                        callOTPForAadhaarVerificationApi((String) binding.sendOtpBtn.getTag(), mobileNo, binding.otpBox.getText().toString());
                    }
                }
            }
        });
    }

    private void checkInternetConnection() {
        if (!NetworkConnection.isOnline(context)) {    // no internet.
            showOKDialog(context,ContextCompat.getDrawable(context,R.drawable.ui2_ic_warning_internet),
                    getString(R.string.error_network), getString(R.string.you_need_an_active_internet_connection_to_use_this_feature),
                    getString(R.string.ok), action -> {
                        if (action == DialogUtils.CustomDialogListener.POSITIVE_CLICK) {
                            finish();
                        }
                    });
        }
    }

    /**
     * This function is used to handle the resend counter and the necessary text to be displayed.
     */
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
                            Timber.tag(TAG).d("onSuccess: TokenResponse: %s", tokenResponse1.toString());
                            if (accessToken.isEmpty()) {    // if token empty
                                Toast.makeText(context, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                                cancelResendAndHideView();
                                return;
                            }
                             callAadhaarMobileVerificationApi(accessToken);
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


    private void callAadhaarMobileVerificationApi(String accessToken) {
        // payload
        AadharApiBody aadharApiBody = new AadharApiBody();
        String aadhaarNo;
        aadhaarNo = Objects.requireNonNull(binding.layoutDoNotHaveABHANumber.aadharNoBox.getText()).toString().trim();

        aadharApiBody.setScope(ABDMConstant.SCOPE_AADHAAR);
        aadharApiBody.setValue(aadhaarNo);
        String url = UrlModifiers.getAadharOTPVerificationUrl();

        Single<OTPResponse> responseBodySingle = AppConstants.apiInterface.GET_OTP_FOR_AADHAR(url, accessToken, aadharApiBody);
        new Thread(() -> {
            // api - start
            responseBodySingle.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new DisposableSingleObserver<>() {
                        @Override
                        public void onSuccess(OTPResponse otpResponse) {
                            cpd.dismiss();
                            snackbarUtils.showSnackLinearLayoutParentSuccess(context, binding.layoutParent,
                                    StringUtils.getMessageTranslated(otpResponse.getMessage(), sessionManager.getAppLanguage()), true);

                            Timber.tag(TAG).d("onSuccess: AadhaarResponse: %s", otpResponse.toString());
                            // here, we will receive: txtID, otp
                            // and we need to pass to another api: otp, mobileNo and txtID will go in Header.

                            if (binding.flOtpBox.getVisibility() != View.VISIBLE) {
                                binding.flOtpBox.setVisibility(View.VISIBLE);
                                binding.rlResendOTP.setVisibility(View.VISIBLE);
                                binding.llResendCounter.setVisibility(View.VISIBLE);
                                binding.resendBtn.setPaintFlags(binding.resendBtn.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                            }

                            binding.sendOtpBtn.setTag(otpResponse.getTxnId());
                            binding.sendOtpBtn.setText(getString(R.string.verify));
                            binding.sendOtpBtn.setEnabled(true);    // btn enabled -> since otp is received.
                        }

                        @Override
                        public void onError(Throwable e) {
                            Timber.tag(TAG).e("onError: AadhaarResponse: %s", e.getMessage());
                            Toast.makeText(context, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                            binding.sendOtpBtn.setEnabled(true);
                            binding.sendOtpBtn.setText(R.string.send_otp);  // Send otp.
                            binding.otpBox.setText("");
                            cancelResendAndHideView();
                            cpd.dismiss();
                        }
                    });
            // api - end
        }).start();

    }


    /**
     * Here, this function is used to call the EnrollByAadhaar api which takes @BODY: txtId, mobileNo, otp and will return us
     * patient's details.
     *
     * @param txnId    get from aadhaar card verification api
     * @param mobileNo user which enter
     * @param otp      get from aadhaar card verification api
     */
    private void callOTPForAadhaarVerificationApi(String txnId, String mobileNo, String otp) {
        cpd = new CustomProgressDialog(context);
        cpd.show(getString(R.string.verifying_otp));
        Timber.tag("callOTPForVerificationApi: ").d("parameters: " + txnId + ", " + mobileNo + ", " + otp);

        binding.sendOtpBtn.setEnabled(false);    // btn disabled.
        binding.sendOtpBtn.setTag(null);    // resetting...

        // payload
        String url = UrlModifiers.getOTPForVerificationUrl();
        OTPVerificationRequestBody requestBody = new OTPVerificationRequestBody();
        requestBody.setOtp(otp);
        requestBody.setTxnId(txnId);
        requestBody.setMobileNo(mobileNo);

        Single<OTPVerificationResponse> otpVerificationResponseObservable =
                AppConstants.apiInterface.PUSH_OTP_FOR_VERIFICATION(url, accessToken, requestBody);

        new Thread(() -> {
            // api - start
            otpVerificationResponseObservable
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new DisposableSingleObserver<>() {
                        @Override
                        public void onSuccess(OTPVerificationResponse otpVerificationResponse) {
                            cpd.dismiss();
                            Timber.tag("callOTPForVerificationApi: ").d("onSuccess: %s", otpVerificationResponse.toString());

                            String mobile = otpVerificationResponse.getABHAProfile().getMobile();
                            boolean isMobileEmpty = TextUtils.isEmpty(mobile);
                            boolean isNewUser = otpVerificationResponse.getIsNew();

                            if (isMobileEmpty || !mobile.equalsIgnoreCase(mobileNo)) {
                                MobileNumberOtpVerificationDialog dialog = new MobileNumberOtpVerificationDialog();
                                dialog.openMobileNumberVerificationDialog(accessToken, otpVerificationResponse.getTxnId(), mobileNo, onMobileEnrollCompleted -> {
                                    otpVerificationResponse.getABHAProfile().setMobile(mobileNo);
                                    handleUserFlow(otpVerificationResponse, accessToken, isNewUser);
                                });
                                dialog.show(getSupportFragmentManager(), "");
                            } else {
                                handleUserFlow(otpVerificationResponse, accessToken, isNewUser);
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            binding.sendOtpBtn.setEnabled(true);
                            binding.sendOtpBtn.setText(R.string.send_otp);  // Send otp.
                            binding.otpBox.setText("");
                            cpd.dismiss();
                            Timber.tag("callOTPForVerificationApi: ").e("onError: %s", e.toString());
                            Toast.makeText(context, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                            cancelResendAndHideView();
                        }
                    });
            // api - end
        }).start();

    }

    private void handleUserFlow(OTPVerificationResponse otpVerificationResponse, String accessToken, boolean isNewUser) {
        if (isNewUser) {
            // New user -> fetch address suggestions and navigate to ABHA address screen.
            callFetchAbhaAddressSuggestionsApi(otpVerificationResponse, accessToken);
        } else {
            // Existing user -> check user existence.
            checkIsUserExist(otpVerificationResponse.getABHAProfile().getPhrAddress().get(0), otpVerificationResponse);
        }
    }

    private void callFetchAbhaAddressSuggestionsApi(OTPVerificationResponse otpVerificationResponse, String accessToken) {
        ArrayList<String> addressList = new ArrayList<>();
        // api - start
        String url = UrlModifiers.getEnrollABHASuggestionUrl();
        EnrollSuggestionRequestBody body = new EnrollSuggestionRequestBody();
        body.setTxnId(otpVerificationResponse.getTxnId());

        Single<EnrollSuggestionResponse> enrollSuggestionResponseSingle =
                AppConstants.apiInterface.PUSH_ENROLL_ABHA_ADDRESS_SUGGESTION(url, accessToken, body);
        new Thread(() -> enrollSuggestionResponseSingle
                .observeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSingleObserver<>() {
                    @Override
                    public void onSuccess(EnrollSuggestionResponse enrollSuggestionResponse) {
                        Timber.tag(TAG).d("onSuccess: suggestion: %s", enrollSuggestionResponse);
                        if (enrollSuggestionResponse.getAbhaAddressList() != null) {

                            // auto-generated abha preferred address from abdm end.
                            addressList.addAll(otpVerificationResponse.getABHAProfile().getPhrAddress());
                            addressList.addAll(enrollSuggestionResponse.getAbhaAddressList());

                            if (addressList.size() > 0) {
                                Intent intent = new Intent(context, AbhaAddressSuggestionsActivity.class);
                                intent.putStringArrayListExtra("addressList", addressList);
                                intent.putExtra("payload", otpVerificationResponse);
                                intent.putExtra("accessToken", accessToken);
                                startActivity(intent);
                                finish();
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.tag(TAG).e("onError: suggestion%s", e.toString());
                    }
                })).start();
        // api - end

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
        boolean isValid = true;

        if (Objects.requireNonNull(binding.layoutDoNotHaveABHANumber.aadharNoBox.getText()).toString().isEmpty()) {
            binding.layoutDoNotHaveABHANumber.aadharError.setVisibility(View.VISIBLE);
            binding.layoutDoNotHaveABHANumber.aadharError.setText(getString(R.string.error_field_required));
            binding.layoutDoNotHaveABHANumber.aadharNoBox.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));
            isValid = false;
        } else { // ie. aadhaar no empty
            if (binding.layoutDoNotHaveABHANumber.aadharNoBox.getText().toString().length() < 12) {
                binding.layoutDoNotHaveABHANumber.aadharError.setVisibility(View.VISIBLE);
                binding.layoutDoNotHaveABHANumber.aadharError.setText(getString(R.string.enter_12_digits));
                binding.layoutDoNotHaveABHANumber.aadharNoBox.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));
                isValid = false;
            } else if (!validateAadhaarNumber(binding.layoutDoNotHaveABHANumber.aadharNoBox.getText().toString())) {
                binding.layoutDoNotHaveABHANumber.aadharError.setVisibility(View.VISIBLE);
                binding.layoutDoNotHaveABHANumber.aadharError.setText(R.string.aadhar_number_is_not_valid);
                binding.layoutDoNotHaveABHANumber.aadharNoBox.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));
                isValid = false;
            } else {
                binding.layoutDoNotHaveABHANumber.aadharError.setVisibility(View.GONE);
                binding.layoutDoNotHaveABHANumber.aadharNoBox.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_input_fieldnew));
            }
        }
        if (Objects.requireNonNull(binding.layoutDoNotHaveABHANumber.mobileNoBox.getText()).toString().isEmpty()) {
            binding.layoutDoNotHaveABHANumber.mobileError.setVisibility(View.VISIBLE);
            binding.layoutDoNotHaveABHANumber.mobileError.setText(getString(R.string.error_field_required));
            binding.layoutDoNotHaveABHANumber.mobileNoBox.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));
            isValid = false;
        } else {
            if (binding.layoutDoNotHaveABHANumber.mobileNoBox.getText().toString().length() < 10) {
                binding.layoutDoNotHaveABHANumber.mobileError.setVisibility(View.VISIBLE);
                binding.layoutDoNotHaveABHANumber.mobileError.setText(getString(R.string.enter_10_digits));
                binding.layoutDoNotHaveABHANumber.mobileNoBox.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));
                isValid = false;
            } else {
                binding.layoutDoNotHaveABHANumber.mobileError.setVisibility(View.GONE);
                binding.layoutDoNotHaveABHANumber.mobileNoBox.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_input_fieldnew));
            }
        }
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

    private void checkIsUserExist(String abhaAddress, OTPVerificationResponse abhaProfileResponse) {

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
                            abhaProfileResponse.setUuID(response.getData().getUuid());
                            intent = new Intent(context, IdentificationActivity_New.class);
                            intent.putExtra(PAYLOAD, abhaProfileResponse);
                            intent.putExtra("accessToken", accessToken);
                            intent.putExtra("patient_detail", true);
                            startActivity(intent);
                        } else {
                            intent = new Intent(context, IdentificationActivity_New.class);
                            intent.putExtra(PAYLOAD, abhaProfileResponse);
                            intent.putExtra("accessToken", accessToken);
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
                String time = resendTime + " " + millisUntilFinished / 1000 + " " + getResources().getString(R.string.seconds);
                binding.resendBtn.setText(time);
                Timber.tag(TAG).d("onTick: %s", time);
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