package org.intelehealth.app.abdm.activity;

import static org.intelehealth.app.utilities.DialogUtils.showOKDialog;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.LocaleList;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.intelehealth.app.R;
import org.intelehealth.app.abdm.model.AadharApiBody;
import org.intelehealth.app.abdm.model.AbhaCardResponseBody;
import org.intelehealth.app.abdm.model.AbhaProfileRequestBody;
import org.intelehealth.app.abdm.model.AbhaProfileResponse;
import org.intelehealth.app.abdm.model.EnrollSuggestionRequestBody;
import org.intelehealth.app.abdm.model.EnrollSuggestionResponse;
import org.intelehealth.app.abdm.model.MobileLoginApiBody;
import org.intelehealth.app.abdm.model.MobileLoginOnOTPVerifiedResponse;
import org.intelehealth.app.abdm.model.OTPResponse;
import org.intelehealth.app.abdm.model.OTPVerificationRequestBody;
import org.intelehealth.app.abdm.model.OTPVerificationResponse;
import org.intelehealth.app.abdm.model.TokenResponse;
import org.intelehealth.app.activities.identificationActivity.IdentificationActivity_New;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.databinding.ActivityAadharMobileVerificationBinding;
import org.intelehealth.app.utilities.DialogUtils;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.SnackbarUtils;
import org.intelehealth.app.utilities.StringUtils;
import org.intelehealth.app.utilities.UrlModifiers;
import org.intelehealth.app.utilities.WindowsUtils;
import org.intelehealth.app.widget.materialprogressbar.CustomProgressDialog;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class AadharMobileVerificationActivity extends AppCompatActivity {
    private Context context = AadharMobileVerificationActivity.this;
    public static final String TAG = AadharMobileVerificationActivity.class.getSimpleName();
    public static String SCOPE = "mobile";
    ActivityAadharMobileVerificationBinding binding;
    private String accessToken = "";
    String optionSelected = "username";
    private boolean hasABHA, abhaCard;
    public static final String BEARER_AUTH = "Bearer ";
    private CustomProgressDialog cpd;
    SnackbarUtils snackbarUtils;
    SessionManager sessionManager = null;
    private CountDownTimer countDownTimer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAadharMobileVerificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        WindowsUtils.setStatusBarColor(AadharMobileVerificationActivity.this);  // changing status bar color
        cpd = new CustomProgressDialog(context);
        snackbarUtils = new SnackbarUtils();
        sessionManager = new SessionManager(context);

        binding.ivBackArrow.setOnClickListener(v -> {
            finish();
        });

        // check internet - start
        if (!NetworkConnection.isOnline(context)) {    // no internet.
            showOKDialog(context, getDrawable(R.drawable.ui2_ic_warning_internet),
                    getString(R.string.error_network), getString(R.string.you_need_an_active_internet_connection_to_use_this_feature),
                    getString(R.string.ok), new DialogUtils.CustomDialogListener() {
                        @Override
                        public void onDialogActionDone(int action) {
                            if (action == DialogUtils.CustomDialogListener.POSITIVE_CLICK) {
                                finish();
                            }
                        }
                    });
        }
        // check internet - end

        Intent intent = getIntent();
        hasABHA = intent.getBooleanExtra("hasABHA", false);
        abhaCard = intent.getBooleanExtra("abhaCard", false);
        Log.d(TAG, "hasABHA: " + hasABHA + " : " + abhaCard);

        if (hasABHA) {
            binding.layoutDoNotHaveABHANumber.flDoNotHaveABHANumber.setVisibility(View.GONE);
            binding.layoutHaveABHANumber.flHaveABHANumber.setVisibility(View.VISIBLE);

            if (abhaCard)
                binding.layoutHaveABHANumber.buttonMobileNumber.setVisibility(View.GONE);
            else
                binding.layoutHaveABHANumber.buttonMobileNumber.setVisibility(View.VISIBLE);

            clickListenerFor_HasABHA();
        } else {
            binding.layoutDoNotHaveABHANumber.flDoNotHaveABHANumber.setVisibility(View.VISIBLE);
            binding.layoutHaveABHANumber.flHaveABHANumber.setVisibility(View.GONE);
        }

        binding.resendBtn.setOnClickListener(v -> {
            resendOtp();
            binding.otpBox.setText("");
            callGenerateTokenApi();
        });

        binding.sendOtpBtn.setOnClickListener(v -> {
            if (checkValidation()) {
                if (binding.flOtpBox.getVisibility() != View.VISIBLE) {
                    binding.flOtpBox.setVisibility(View.VISIBLE);
                    binding.rlResendOTP.setVisibility(View.VISIBLE);
                    binding.resendBtn.setPaintFlags(binding.resendBtn.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                }

                if (binding.sendOtpBtn.getTag() == null) {  // ie. fresh call - sending otp.
                    resendOtp();
                    callGenerateTokenApi();
                } else {
                    // ie. otp received and making call to enrollAadhar api.
                    if (binding.otpBox.getText().toString().isEmpty()) {    // ie. OTP not entered in box.
                        snackbarUtils.showSnackLinearLayoutParentSuccess(context, binding.layoutParent,
                                StringUtils.getMessageTranslated(getString(R.string.please_enter_otp_received), sessionManager.getAppLanguage()), false);
                        return;
                    }

                    if (!binding.otpBox.getText().toString().isEmpty()) {
                        String mobileNo = "";
                        if (hasABHA) {
                            if (optionSelected.equalsIgnoreCase("mobile")) {
                                // via. mobile login
                                mobileNo = binding.layoutHaveABHANumber.edittextMobileNumber.getText().toString().trim();
                                callOTPForMobileLoginVerificationApi((String) binding.sendOtpBtn.getTag(), binding.otpBox.getText().toString());
                            }
                            else {
                                if (abhaCard) {
                                    callOTPForMobileLoginVerificationApi((String) binding.sendOtpBtn.getTag(), binding.otpBox.getText().toString());
                                }
                                else {
                                    // via. aadhar login
                                    mobileNo = binding.layoutHaveABHANumber.edittextMobileNumber.getText().toString().trim();
                                    callOTPForVerificationApi((String) binding.sendOtpBtn.getTag(), mobileNo, binding.otpBox.getText().toString());
                                }
                            }
                        } else {
                            mobileNo = binding.layoutDoNotHaveABHANumber.mobileNoBox.getText().toString().trim();
                            callOTPForVerificationApi((String) binding.sendOtpBtn.getTag(), mobileNo, binding.otpBox.getText().toString());
                        }
                    }
                }
            }
        });

        //  resendOtp();
    }

    private void clickListenerFor_HasABHA() {
        binding.otpBox.setText("");

        binding.layoutHaveABHANumber.buttonUsername.setOnClickListener(v -> {
            optionSelected = "username";
            binding.layoutHaveABHANumber.edittextUsername.setText("");
            binding.layoutHaveABHANumber.edittextMobileNumber.setText("");
            binding.layoutHaveABHANumber.layoutParentMobileNo.setVisibility(View.VISIBLE);  // show mobile no as well for aadhar as api requires it.
            binding.layoutHaveABHANumber.layoutParentUsername.setVisibility(View.VISIBLE);
            binding.layoutHaveABHANumber.tvMobileError.setVisibility(View.GONE);
            binding.layoutHaveABHANumber.tvUsernameError.setVisibility(View.GONE);
            binding.layoutHaveABHANumber.buttonUsername.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_bg_forgot_pass_ui2));
            binding.layoutHaveABHANumber.buttonMobileNumber.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_bg_forgot_pass_disabled_ui2));
            binding.layoutHaveABHANumber.edittextMobileNumber.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_input_fieldnew));
            binding.layoutHaveABHANumber.edittextUsername.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_input_fieldnew));
        });

        binding.layoutHaveABHANumber.buttonMobileNumber.setOnClickListener(v -> {
            optionSelected = "mobile";
            binding.layoutHaveABHANumber.edittextUsername.setText("");
            binding.layoutHaveABHANumber.layoutParentUsername.setVisibility(View.GONE);
            binding.layoutHaveABHANumber.layoutParentMobileNo.setVisibility(View.VISIBLE);
            binding.layoutHaveABHANumber.tvMobileError.setVisibility(View.GONE);
            binding.layoutHaveABHANumber.tvUsernameError.setVisibility(View.GONE);
            binding.layoutHaveABHANumber.buttonMobileNumber.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_bg_forgot_pass_ui2));
            binding.layoutHaveABHANumber.buttonUsername.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_bg_forgot_pass_disabled_ui2));
            binding.layoutHaveABHANumber.edittextMobileNumber.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_input_fieldnew));
            binding.layoutHaveABHANumber.edittextUsername.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_input_fieldnew));
        });

        if (abhaCard) {
            optionSelected = "username";
            binding.layoutHaveABHANumber.edittextUsername.setText("");
          //  binding.layoutHaveABHANumber.edittextMobileNumber.setText("");
            binding.layoutHaveABHANumber.layoutParentMobileNo.setVisibility(View.GONE);  // in case of card, mobile no is not required...
            binding.layoutHaveABHANumber.layoutParentUsername.setVisibility(View.VISIBLE);
      //      binding.layoutHaveABHANumber.tvMobileError.setVisibility(View.GONE);
            binding.layoutHaveABHANumber.tvUsernameError.setVisibility(View.GONE);
            binding.layoutHaveABHANumber.buttonUsername.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_bg_forgot_pass_ui2));
        //    binding.layoutHaveABHANumber.buttonMobileNumber.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_bg_forgot_pass_disabled_ui2));
        //    binding.layoutHaveABHANumber.edittextMobileNumber.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_input_fieldnew));
            binding.layoutHaveABHANumber.edittextUsername.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_input_fieldnew));
        }
    }

    private void callGenerateTokenApi() {   // Step 1.
        cpd.show(getString(R.string.otp_sending));
        binding.sendOtpBtn.setEnabled(false);    // btn disabled.
        binding.sendOtpBtn.setTag(null);    // resetting...

        Single<TokenResponse> tokenResponse = AppConstants.apiInterface.GET_TOKEN(UrlModifiers.getABDM_TokenUrl());
        new Thread(new Runnable() {
            @Override
            public void run() {
                // api - start
                tokenResponse.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new DisposableSingleObserver<TokenResponse>() {
                            @Override
                            public void onSuccess(TokenResponse tokenResponse) {
                                accessToken = BEARER_AUTH + tokenResponse.getAccessToken();
                                Log.d(TAG, "onSuccess: TokenResponse: " + tokenResponse.toString());
                                if (accessToken.isEmpty()) {    // if token empty
                                    Toast.makeText(context, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                                    cancelResendAndHideView();
                                    return;
                                }

                                //   binding.sendOtpBtn.setTag("d4933b4b-0d08-43f5-b699-c588db8742c9"); // todo testing purpose...
                                if (hasABHA) {
                                    if (abhaCard) {
                                        // call mobile api.
                                        callMobileNumberVerificationApi(accessToken);
                                    }
                                    else {
                                        if (!optionSelected.isEmpty() && optionSelected.equalsIgnoreCase("username")) {
                                            callAadharMobileVerificationApi(accessToken);   // via. aadharEnroll api
                                        } else if (!optionSelected.isEmpty() && optionSelected.equalsIgnoreCase("mobile")) {
                                            // call mobile api.
                                            callMobileNumberVerificationApi(accessToken);
                                        }
                                    }
                                } else {
                                    callAadharMobileVerificationApi(accessToken);   // via. aadharEnroll api
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                Toast.makeText(context, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "onError: callGenerateTokenApi: " + e.toString());
                                binding.sendOtpBtn.setEnabled(true);
                                cancelResendAndHideView();
                                cpd.dismiss();
                            }
                        });
                // api - end
            }
        }).start();

    }

    private void callMobileNumberVerificationApi(String accessToken) {  // mobile: Step 2
        // payload - start
        MobileLoginApiBody mobileLoginApiBody = new MobileLoginApiBody();
        if (abhaCard) {
            SCOPE = "aadhar";
            mobileLoginApiBody.setValue(Objects.requireNonNull(binding.layoutHaveABHANumber.edittextUsername.getText()).toString().trim()); // aadhar value.
        }
        else
            mobileLoginApiBody.setValue(Objects.requireNonNull(binding.layoutHaveABHANumber.edittextMobileNumber.getText()).toString().trim()); // mobile value.

        mobileLoginApiBody.setScope(SCOPE);
        String url = UrlModifiers.getMobileLoginVerificationUrl();
        // payload - end

        Single<OTPResponse> mobileResponseSingle = AppConstants.apiInterface.GET_OTP_FOR_MOBILE(url, accessToken, mobileLoginApiBody);
        new Thread(new Runnable() {
            @Override
            public void run() {
                // api - start
                mobileResponseSingle
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new DisposableSingleObserver<OTPResponse>() {
                            @Override
                            public void onSuccess(OTPResponse otpResponse) {
                                cpd.dismiss();
                                snackbarUtils.showSnackLinearLayoutParentSuccess(context, binding.layoutParent,
                                        StringUtils.getMessageTranslated(otpResponse.getMessage(), sessionManager.getAppLanguage()), true);

                                Log.d(TAG, "onSuccess: callMobileNumberVerificationApi: " + otpResponse.toString());
                                // here, we will receive: txtID and otp will be received via SMS.
                                // and we need to pass to another api: otp, mobileNo and txtID will go in Header.

                                if (binding.flOtpBox.getVisibility() != View.VISIBLE) {
                                    binding.flOtpBox.setVisibility(View.VISIBLE);
                                    binding.rlResendOTP.setVisibility(View.VISIBLE);
                                    binding.resendBtn.setPaintFlags(binding.resendBtn.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                                }

                                binding.sendOtpBtn.setTag(otpResponse.getTxnId());
                                binding.sendOtpBtn.setText(getString(R.string.verify));
                                binding.sendOtpBtn.setEnabled(true);    // btn enabled -> since otp is received.
                            }

                            @Override
                            public void onError(Throwable e) {
                                binding.sendOtpBtn.setEnabled(true);
                                binding.otpBox.setText("");
                                Log.e(TAG, "onError: callMobileNumberVerificationApi: " + e.getMessage());
                                Toast.makeText(context, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                                cancelResendAndHideView();
                                cpd.dismiss();
                            }
                        });
                // api - end
            }
        }).start();
    }

    private void callAadharMobileVerificationApi(String accessToken) {
        // payload
        AadharApiBody aadharApiBody = new AadharApiBody();
        String aadharNo = "";
        if (hasABHA)
            aadharNo = binding.layoutHaveABHANumber.edittextUsername.getText().toString().trim();
        else
            aadharNo = binding.layoutDoNotHaveABHANumber.aadharNoBox.getText().toString().trim();

        aadharApiBody.setAadhar(aadharNo);
        String url = UrlModifiers.getAadharOTPVerificationUrl();

        Single<OTPResponse> responseBodySingle = AppConstants.apiInterface.GET_OTP_FOR_AADHAR(url, accessToken, aadharApiBody);
        new Thread(new Runnable() {
            @Override
            public void run() {
                // api - start
                responseBodySingle.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new DisposableSingleObserver<OTPResponse>() {
                            @Override
                            public void onSuccess(OTPResponse otpResponse) {
                                cpd.dismiss();
                                snackbarUtils.showSnackLinearLayoutParentSuccess(context, binding.layoutParent,
                                        StringUtils.getMessageTranslated(otpResponse.getMessage(), sessionManager.getAppLanguage()), true);

                                Log.d(TAG, "onSuccess: AadharResponse: " + otpResponse.toString());
                                // here, we will receive: txtID, otp
                                // and we need to pass to another api: otp, mobileNo and txtID will go in Header.

                                if (binding.flOtpBox.getVisibility() != View.VISIBLE) {
                                    binding.flOtpBox.setVisibility(View.VISIBLE);
                                    binding.rlResendOTP.setVisibility(View.VISIBLE);
                                    binding.resendBtn.setPaintFlags(binding.resendBtn.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                                }

                                binding.sendOtpBtn.setTag(otpResponse.getTxnId());
                                binding.sendOtpBtn.setText(getString(R.string.verify));
                                binding.sendOtpBtn.setEnabled(true);    // btn enabled -> since otp is received.
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.e(TAG, "onError: AadharResponse: " + e.getMessage());
                                Toast.makeText(context, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                                binding.sendOtpBtn.setEnabled(true);
                                binding.otpBox.setText("");
                                cancelResendAndHideView();
                                cpd.dismiss();
                            }
                        });
                // api - end
            }
        }).start();

    }

    /**
     * Here, this function will only be called if user has ABHA number and he wants to use the login via. Mobile login flow.
     *
     * @param txnId: txnId received in success response.
     * @param otp    : otp received via. SMS
     */
    private void callOTPForMobileLoginVerificationApi(String txnId, String otp) {   // Mobile: Step 3
        cpd = new CustomProgressDialog(context);
        cpd.show("Verifying OTP...");
        Log.d("callOTPForVerificationApi: ", "parameters: " + txnId + ", " + otp);
        binding.sendOtpBtn.setEnabled(false);    // btn disabled.
        binding.sendOtpBtn.setTag(null);    // resetting...

        // payload
        String url = UrlModifiers.getOTPForMobileLoginVerificationUrl();
        OTPVerificationRequestBody requestBody = new OTPVerificationRequestBody();
        requestBody.setTxnId(txnId);
        requestBody.setOtp(otp);

        if (abhaCard)
            SCOPE = "aadhar";

        requestBody.setScope(SCOPE);
        Single<MobileLoginOnOTPVerifiedResponse> mobileLoginOnOTPVerifiedResponseSingle =
                AppConstants.apiInterface.PUSH_OTP_FOR_MOBILE_LOGIN_VERIFICATION(url, accessToken, requestBody);
        new Thread(new Runnable() {
            @Override
            public void run() {
                mobileLoginOnOTPVerifiedResponseSingle
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new DisposableSingleObserver<MobileLoginOnOTPVerifiedResponse>() {
                            @Override
                            public void onSuccess(MobileLoginOnOTPVerifiedResponse mobileLoginOnOTPVerifiedResponse) {
                                cpd.dismiss();

                                Log.d("callOTPForMobileLoginVerificationApi", "onSuccess: " + mobileLoginOnOTPVerifiedResponse.toString());
                                if (mobileLoginOnOTPVerifiedResponse.getAccounts() != null) {

                                    // TODO: Handle abha card implementation here.... 16th Feb - start
                                    if (abhaCard) {
                                        String X_TOKEN = BEARER_AUTH + mobileLoginOnOTPVerifiedResponse.getToken();
                                        callGETAbhaCardApi(X_TOKEN, accessToken, mobileLoginOnOTPVerifiedResponse);
                                    }
                                    else {
                                        // TODO: Handle abha card implementation here.... 16th Feb - end

                                        if (mobileLoginOnOTPVerifiedResponse.getAccounts().size() > 0) {    // ie. there is atleast one (1) account.

                                            if (mobileLoginOnOTPVerifiedResponse.getAccounts().size() > 1) {
                                                // ie. there are more than 1 accounts for this mobile number than show -> Accounts selection screen.
                                                Intent intent = new Intent(context, AccountSelectionLoginActivity.class);
                                                String X_TOKEN = BEARER_AUTH + mobileLoginOnOTPVerifiedResponse.getToken();

                                                intent.putExtra("X_TOKEN", X_TOKEN);
                                                intent.putExtra("payload", mobileLoginOnOTPVerifiedResponse);
                                                intent.putExtra("accessToken", accessToken);
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                // ie. Only 1 account for this mobile number than call -> fetch User Profile details api.
                                                String ABHA_NUMBER = mobileLoginOnOTPVerifiedResponse.getAccounts().get(0).getABHANumber();
                                                String X_TOKEN = BEARER_AUTH + mobileLoginOnOTPVerifiedResponse.getToken();
                                                callFetchUserProfileAPI(ABHA_NUMBER, mobileLoginOnOTPVerifiedResponse.getTxnId(), X_TOKEN);
                                            }

                                        }
                                    }
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                cpd.dismiss();
                                Log.e("callOTPForMobileLoginVerificationApi", "onError: " + e.toString());
                                Toast.makeText(context, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                                binding.sendOtpBtn.setEnabled(true);
                                binding.otpBox.setText("");
                                cancelResendAndHideView();
                            }
                        });
            }
        }).start();
    }

    /**
     * This api is used to call the GET Abha card api which returns a base64 encoded image.
     *
     * @param xToken
     * @param accessToken
     * @param mobileLoginOnOTPVerifiedResponse
     */
    private void callGETAbhaCardApi(String xToken, String accessToken, MobileLoginOnOTPVerifiedResponse mobileLoginOnOTPVerifiedResponse) {
        Log.d(TAG, "callGETAbhaCardApi: " + accessToken + " : " + xToken);
        String url = UrlModifiers.getABHACardUrl();
        Single<AbhaCardResponseBody> responseBodySingle = AppConstants.apiInterface.GET_ABHA_CARD(url, accessToken, xToken);
        new Thread(new Runnable() {
            @Override
            public void run() {
                responseBodySingle
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new DisposableSingleObserver<AbhaCardResponseBody>() {
                            @Override
                            public void onSuccess(AbhaCardResponseBody abhaCardResponseBody) {
                                if (abhaCardResponseBody != null) {
                                    Log.d("callGETAbhaCardApi", "onSuccess: " + abhaCardResponseBody.toString());

                                    // TODO: here it will return base64 encoded image.
                                    Intent intent = new Intent(context, AbhaCardActivity.class);
                                    intent.putExtra("payload", abhaCardResponseBody);
                                    intent.putExtra("data", mobileLoginOnOTPVerifiedResponse);
                                    startActivity(intent);
                                    finish();
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.e("callGETAbhaCardApi", "onError: " + e.toString());
                                cpd.dismiss();
                                Toast.makeText(context, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                                binding.sendOtpBtn.setEnabled(true);
                                binding.otpBox.setText("");
                                cancelResendAndHideView();
                            }
                        });
            }
        }).start();
    }

    /**
     * This will call the Fetch Profile Details api to fetch the details related to this user.
     *
     * @param abhaNumber
     * @param txnId
     * @param xToken
     */
    private void callFetchUserProfileAPI(String abhaNumber, String txnId, String xToken) {
        binding.sendOtpBtn.setEnabled(false);    // btn disabled.
        binding.sendOtpBtn.setTag(null);    // resetting...

        // todo: call fetch abha profile api here.
        // Toast.makeText(context, "Mobile login is working...", Toast.LENGTH_SHORT).show();

        // payload - start
        String url = UrlModifiers.getABHAProfileUrl();
        AbhaProfileRequestBody requestBody = new AbhaProfileRequestBody();
        requestBody.setTxnId(txnId);
        requestBody.setAbhaNumber(abhaNumber);
        // payload - end

        Single<AbhaProfileResponse> abhaProfileResponseSingle =
                AppConstants.apiInterface.PUSH_ABHA_PROFILE(url, accessToken, xToken, requestBody);
        new Thread(new Runnable() {
            @Override
            public void run() {
                abhaProfileResponseSingle
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new DisposableSingleObserver<AbhaProfileResponse>() {
                            @Override
                            public void onSuccess(AbhaProfileResponse abhaProfileResponse) {
                                Log.d("callFetchUserProfileAPI", "onSuccess: " + abhaProfileResponse);
                                Intent intent;
                                // ie. only 1 account exists.
                                intent = new Intent(context, IdentificationActivity_New.class);
                                intent.putExtra("mobile_payload", abhaProfileResponse);
                                intent.putExtra("accessToken", accessToken);
                                startActivity(intent);
                                finish();
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.e("callFetchUserProfileAPI", "onError: " + e.toString());
                            }
                        });
            }
        }).start();

    }


    /**
     * Here, this function is used to call the EnrollByAadhar api which takes @BODY: txtId, mobileNo, otp and will return us
     * patient's details.
     *
     * @param txnId
     * @param mobileNo
     * @param otp
     */
    private void callOTPForVerificationApi(String txnId, String mobileNo, String otp) {
        cpd = new CustomProgressDialog(context);
        cpd.show("Verifying OTP...");
        Log.d("callOTPForVerificationApi: ", "parameters: " + txnId + ", " + mobileNo + ", " + otp);

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

        new Thread(new Runnable() {
            @Override
            public void run() {
                // api - start
                otpVerificationResponseObservable
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new DisposableSingleObserver<OTPVerificationResponse>() {
                            @Override
                            public void onSuccess(OTPVerificationResponse otpVerificationResponse) {
                                cpd.dismiss();

                                // 1. if new user than isNew = true
                                // 2. if already exist user than isNew = false.
                                Log.d("callOTPForVerificationApi: ", "onSuccess: " + otpVerificationResponse.toString());

                                if (otpVerificationResponse.getIsNew()) {
                                    //   if (!otpVerificationResponse.getIsNew()) {  // todo: testing -> comment later and uncomment above.
                                    // New user -> than fetch address suggestions and take to ABHA address screen.
                                    callFetchAbhaAddressSuggestionsApi(otpVerificationResponse, accessToken);
                                } else {
                                    // Already user exist -> than take to Patient Registration screen.
                                    Intent intent = new Intent(context, IdentificationActivity_New.class);
                                    intent.putExtra("payload", otpVerificationResponse);
                                    intent.putExtra("accessToken", accessToken);
                                    startActivity(intent);
                                    finish();
                                } // todo: uncomment later.

                                //   intent = new Intent(context, AbhaAddressSuggestionsActivity.class); // todo: remove this later: testing...
                            }

                            @Override
                            public void onError(Throwable e) {
                                binding.sendOtpBtn.setEnabled(true);
                                binding.otpBox.setText("");
                                cpd.dismiss();
                                Log.e("callOTPForVerificationApi: ", "onError: " + e.toString());
                                Toast.makeText(context, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                                cancelResendAndHideView();
                            }
                        });
                // api - end
            }
        }).start();

    }

    private ArrayList<String> callFetchAbhaAddressSuggestionsApi(OTPVerificationResponse otpVerificationResponse, String accessToken) {
        ArrayList<String> addressList = new ArrayList<>();
        // api - start
        String url = UrlModifiers.getEnrollABHASuggestionUrl();
        EnrollSuggestionRequestBody body = new EnrollSuggestionRequestBody();
        body.setTxnId(otpVerificationResponse.getTxnId());

        Single<EnrollSuggestionResponse> enrollSuggestionResponseSingle =
                AppConstants.apiInterface.PUSH_ENROLL_ABHA_ADDRESS_SUGGESTION(url, accessToken, body);
        new Thread(new Runnable() {
            @Override
            public void run() {
                enrollSuggestionResponseSingle
                        .observeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new DisposableSingleObserver<EnrollSuggestionResponse>() {
                            @Override
                            public void onSuccess(EnrollSuggestionResponse enrollSuggestionResponse) {
                                Log.d(TAG, "onSuccess: suggestion: " + enrollSuggestionResponse);
                                if (enrollSuggestionResponse.getAbhaAddressList() != null) {

                                    for (String phrAddressAutoGenerated : otpVerificationResponse.getABHAProfile().getPhrAddress()) { // auto-generated abha preferred address from abdm end.
                                        addressList.add(phrAddressAutoGenerated);
                                    }
                                    for (String phrAddress : enrollSuggestionResponse.getAbhaAddressList()) {
                                        addressList.add(phrAddress);
                                    }

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
                                Log.e(TAG, "onError: suggestion" + e.toString());
                            }
                        });
            }
        }).start();
        // api - end

        return addressList;
    }


    private boolean checkValidation() {
        boolean isValid = true;

        if (hasABHA) {
            isValid = areInputFieldsValid_HasABHA();
        } else {
            if (binding.layoutDoNotHaveABHANumber.aadharNoBox.getText().toString().isEmpty()) {
                binding.layoutDoNotHaveABHANumber.aadharError.setVisibility(View.VISIBLE);
                binding.layoutDoNotHaveABHANumber.aadharError.setText(getString(R.string.error_field_required));
                binding.layoutDoNotHaveABHANumber.aadharNoBox.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));
                isValid = false;
            }
            else { // ie. aadhar no empty
                if (binding.layoutDoNotHaveABHANumber.aadharNoBox.getText().toString().length() < 12) {
                    binding.layoutDoNotHaveABHANumber.aadharError.setVisibility(View.VISIBLE);
                    binding.layoutDoNotHaveABHANumber.aadharError.setText(getString(R.string.enter_12_digits));
                    binding.layoutDoNotHaveABHANumber.aadharNoBox.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));
                    isValid = false;
                }
                else {
                    binding.layoutDoNotHaveABHANumber.aadharError.setVisibility(View.GONE);
                    binding.layoutDoNotHaveABHANumber.aadharNoBox.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_input_fieldnew));
                }
            }
            if (binding.layoutDoNotHaveABHANumber.mobileNoBox.getText().toString().isEmpty()) {
                binding.layoutDoNotHaveABHANumber.mobileError.setVisibility(View.VISIBLE);
                binding.layoutDoNotHaveABHANumber.mobileError.setText(getString(R.string.error_field_required));
                binding.layoutDoNotHaveABHANumber.mobileNoBox.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));
                isValid = false;
            }
            else {
                if (binding.layoutDoNotHaveABHANumber.mobileNoBox.getText().toString().length() < 10) {
                    binding.layoutDoNotHaveABHANumber.mobileError.setVisibility(View.VISIBLE);
                    binding.layoutDoNotHaveABHANumber.mobileError.setText(getString(R.string.enter_10_digits));
                    binding.layoutDoNotHaveABHANumber.mobileNoBox.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));
                    isValid = false;
                }
                else {
                    binding.layoutDoNotHaveABHANumber.mobileError.setVisibility(View.GONE);
                    binding.layoutDoNotHaveABHANumber.mobileNoBox.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_input_fieldnew));
                }
            }
        }

        // common area...
        if (binding.flOtpBox.getVisibility() == View.VISIBLE) {
            if (binding.otpBox.getText() != null) {
                if (binding.otpBox.getText().toString().isEmpty()) {
                    Toast.makeText(context, "Please enter OTP received!", Toast.LENGTH_LONG).show();
                    isValid = false;
                }
            }
        }

        return isValid;
    }

    private boolean areInputFieldsValid_HasABHA() {
        boolean isvalid = true;
        if (!optionSelected.isEmpty() && optionSelected.equals("username")) {

            // aadhar validation - start
            String aadharNo = binding.layoutHaveABHANumber.edittextUsername.getText().toString().replace(" ", "").trim();
            if (aadharNo.isEmpty()) {
                binding.layoutHaveABHANumber.aadharError.setVisibility(View.VISIBLE);
                binding.layoutHaveABHANumber.aadharError.setText(getString(R.string.error_field_required));
                binding.layoutHaveABHANumber.edittextUsername.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));
                isvalid = false;
            }
            else {
                if (aadharNo.length() != 12) {
                    binding.layoutHaveABHANumber.aadharError.setVisibility(View.VISIBLE);
                    binding.layoutHaveABHANumber.aadharError.setText(getString(R.string.enter_12_digits));
                    isvalid = false;
                }
                else {
                    binding.layoutHaveABHANumber.edittextUsername.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_input_fieldnew));
                    binding.layoutHaveABHANumber.aadharError.setVisibility(View.GONE);
                }
            }
            // aadhar validaiton - end

            // mobile for aadhar - start
            String mobile = binding.layoutHaveABHANumber.edittextMobileNumber.getText().toString().replace(" ", "").trim();
            Log.v(TAG, mobile);

            if (!abhaCard) {
                if (mobile.isEmpty()) {
                    binding.layoutHaveABHANumber.mobileError.setVisibility(View.VISIBLE);
                    binding.layoutHaveABHANumber.mobileError.setText(getString(R.string.error_field_required));
                    binding.layoutHaveABHANumber.edittextMobileNumber.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));
                    isvalid = false;
                }
                else {
                    if (/*code.equalsIgnoreCase("91") &&*/ mobile.length() != 10) {
                        binding.layoutHaveABHANumber.mobileError.setVisibility(View.VISIBLE);
                        binding.layoutHaveABHANumber.mobileError.setText(getString(R.string.enter_10_digits));
                        binding.layoutHaveABHANumber.edittextMobileNumber.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));
                        isvalid = false;
                    }
                    else {
                        binding.layoutHaveABHANumber.edittextMobileNumber.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_input_fieldnew));
                        binding.layoutHaveABHANumber.mobileError.setVisibility(View.GONE);
                    }
                }
            }
            // mobile for aadhar - end
        }
        else if (!optionSelected.isEmpty() && optionSelected.equals("mobile")) {  // Phone number field

            String mobile = binding.layoutHaveABHANumber.edittextMobileNumber.getText().toString().replace(" ", "").trim();
            Log.v(TAG, mobile);

            if (mobile.isEmpty()) {
                binding.layoutHaveABHANumber.mobileError.setVisibility(View.VISIBLE);
                binding.layoutHaveABHANumber.mobileError.setText(getString(R.string.error_field_required));
                binding.layoutHaveABHANumber.edittextMobileNumber.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));
                isvalid = false;
            }
            else {
                if (/*code.equalsIgnoreCase("91") &&*/ mobile.length() != 10) {
                    binding.layoutHaveABHANumber.mobileError.setVisibility(View.VISIBLE);
                    binding.layoutHaveABHANumber.mobileError.setText(getString(R.string.enter_10_digits));
                    binding.layoutHaveABHANumber.edittextMobileNumber.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));
                    isvalid = false;
                }
                else {
                    binding.layoutHaveABHANumber.edittextMobileNumber.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_input_fieldnew));
                    binding.layoutHaveABHANumber.mobileError.setVisibility(View.GONE);
                }
            }
        }

        return isvalid;
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

        if (binding.rlResendOTP.getVisibility() == View.VISIBLE)
            binding.rlResendOTP.setVisibility(View.GONE);
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
                Log.d(TAG, "onTick: " + time);
            }

            public void onFinish() {
                binding.resendBtn.setEnabled(true);
                binding.resendBtn.setText(getResources().getString(R.string.resend_otp));
                binding.resendBtn.setTextColor(getColor(R.color.colorPrimary));
                if (cpd != null && cpd.isShowing())
                    cpd.dismiss();
            }

        }.start();
    }



}