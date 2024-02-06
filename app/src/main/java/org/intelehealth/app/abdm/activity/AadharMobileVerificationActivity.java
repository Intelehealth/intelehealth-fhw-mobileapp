package org.intelehealth.app.abdm.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.intelehealth.app.R;
import org.intelehealth.app.abdm.model.AadharApiBody;
import org.intelehealth.app.abdm.model.MobileLoginApiBody;
import org.intelehealth.app.abdm.model.MobileLoginOnOTPVerifiedResponse;
import org.intelehealth.app.abdm.model.OTPResponse;
import org.intelehealth.app.abdm.model.OTPVerificationRequestBody;
import org.intelehealth.app.abdm.model.OTPVerificationResponse;
import org.intelehealth.app.abdm.model.TokenResponse;
import org.intelehealth.app.activities.identificationActivity.IdentificationActivity_New;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.databinding.ActivityAadharMobileVerificationBinding;
import org.intelehealth.app.utilities.UrlModifiers;
import org.intelehealth.app.utilities.WindowsUtils;

import java.util.Objects;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class AadharMobileVerificationActivity extends AppCompatActivity {
    private Context context = AadharMobileVerificationActivity.this;
    public static final String TAG = AadharMobileVerificationActivity.class.getSimpleName();
    public static final String SCOPE = "mobile";
    ActivityAadharMobileVerificationBinding binding;
    private String accessToken = "";
    String optionSelected = "username";
    private boolean hasABHA;
    public static final String BEARER_AUTH = "Bearer ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAadharMobileVerificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        WindowsUtils.setStatusBarColor(AadharMobileVerificationActivity.this);  // changing status bar color

        Intent intent = getIntent();
        hasABHA = intent.getBooleanExtra("hasABHA", false);
        Log.d(TAG, "hasABHA: " + hasABHA);
        if (hasABHA) {
            binding.layoutDoNotHaveABHANumber.flDoNotHaveABHANumber.setVisibility(View.GONE);
            binding.layoutHaveABHANumber.flHaveABHANumber.setVisibility(View.VISIBLE);
            clickListenerFor_HasABHA();
        }
        else {
            binding.layoutDoNotHaveABHANumber.flDoNotHaveABHANumber.setVisibility(View.VISIBLE);
            binding.layoutHaveABHANumber.flHaveABHANumber.setVisibility(View.GONE);
        }

        binding.sendOtpBtn.setOnClickListener(v -> {
            if(checkValidation()) {
                if (binding.flOtpBox.getVisibility() != View.VISIBLE)
                    binding.flOtpBox.setVisibility(View.VISIBLE);

                if (binding.sendOtpBtn.getTag() == null) {  // ie. fresh call - sending otp.
                    callGenerateTokenApi();
                }
                else {
                    // ie. otp received and making call to enrollAadhar api.
                    if (binding.otpBox.getText() != null) {
                        String mobileNo = "";
                        if (hasABHA) {
                            if (optionSelected.equalsIgnoreCase("mobile")) {
                                // via. mobile login
                                mobileNo = binding.layoutHaveABHANumber.edittextMobileNumber.getText().toString().trim();
                                // todo: call mobile login api. no need for aadhar here.
                                callOTPForMobileLoginVerificationApi((String) binding.sendOtpBtn.getTag(), binding.otpBox.getText().toString());
                            }
                            else {
                                // via. aadhar login
                                mobileNo = binding.layoutDoNotHaveABHANumber.mobileNoBox.getText().toString().trim();
                                callOTPForVerificationApi((String) binding.sendOtpBtn.getTag(), mobileNo, binding.otpBox.getText().toString());
                            }
                        }
                        else {
                            mobileNo = binding.layoutDoNotHaveABHANumber.mobileNoBox.getText().toString().trim();
                            callOTPForVerificationApi((String) binding.sendOtpBtn.getTag(), mobileNo, binding.otpBox.getText().toString());
                        }
                    }
                }
            }
        });
    }

    private void clickListenerFor_HasABHA() {
        binding.layoutHaveABHANumber.buttonUsername.setOnClickListener(v -> {
            optionSelected = "username";
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
    }

    private void callGenerateTokenApi() {
        binding.sendOtpBtn.setEnabled(false);    // btn disabled.
     //   binding.sendOtpBtn.setEnabled(true);    // todo: for testing purpose

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
                                    return;
                                }

                                //   binding.sendOtpBtn.setTag("d4933b4b-0d08-43f5-b699-c588db8742c9"); // todo testing purpose...
                                if (hasABHA) {
                                    if (!optionSelected.isEmpty() && optionSelected.equalsIgnoreCase("username")) {
                                        callAadharMobileVerificationApi(accessToken);   // via. aadharEnroll api
                                    }
                                    else if (!optionSelected.isEmpty() && optionSelected.equalsIgnoreCase("mobile")) {
                                        // call mobile api.
                                        callMobileNumberVerificationApi(accessToken);
                                    }
                                }
                                else {
                                    callAadharMobileVerificationApi(accessToken);   // via. aadharEnroll api
                                }
                            }

                            @Override
                            public void onError(Throwable e) {

                            }
                        });
                // api - end
            }
        }).start();

    }

    private void callMobileNumberVerificationApi(String accessToken) {
        // payload - start
        MobileLoginApiBody mobileLoginApiBody = new MobileLoginApiBody();
        mobileLoginApiBody.setScope(SCOPE);
        mobileLoginApiBody.setValue(Objects.requireNonNull(binding.layoutHaveABHANumber.edittextMobileNumber.getText()).toString().trim());
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
                                Log.d(TAG, "onSuccess: callMobileNumberVerificationApi: " + otpResponse.toString());
                                // here, we will receive: txtID and otp will be received via SMS.
                                // and we need to pass to another api: otp, mobileNo and txtID will go in Header.

                                if (binding.flOtpBox.getVisibility() != View.VISIBLE)
                                    binding.flOtpBox.setVisibility(View.VISIBLE);

                                binding.sendOtpBtn.setTag(otpResponse.getTxnId());
                                binding.sendOtpBtn.setText("Verify");
                                binding.sendOtpBtn.setEnabled(true);    // btn enabled -> since otp is received.
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.d(TAG, "onError: callMobileNumberVerificationApi: " + e.getMessage());
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
                                Log.d(TAG, "onSuccess: AadharResponse: " + otpResponse.toString());
                                // here, we will receive: txtID, otp
                                // and we need to pass to another api: otp, mobileNo and txtID will go in Header.

                                if (binding.flOtpBox.getVisibility() != View.VISIBLE)
                                    binding.flOtpBox.setVisibility(View.VISIBLE);

                                binding.sendOtpBtn.setTag(otpResponse.getTxnId());
                                binding.sendOtpBtn.setText("Verify");
                                binding.sendOtpBtn.setEnabled(true);    // btn enabled -> since otp is received.
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.d(TAG, "onError: AadharResponse: " + e.getMessage());
                            }
                        });
                // api - end
            }
        }).start();

    }

    /**
     * Here, this function will only be called if user has ABHA number and he wants to use the login via. Mobile login flow.
     * @param txnId: txnId received in success response.
     * @param otp : otp received via. SMS
     */
    private void callOTPForMobileLoginVerificationApi(String txnId, String otp) {
        Log.d("callOTPForVerificationApi: ", "parameters: " + txnId + ", " + otp);
        binding.sendOtpBtn.setEnabled(false);    // btn disabled.
        binding.sendOtpBtn.setTag(null);    // resetting...

        // payload
        String url = UrlModifiers.getOTPForMobileLoginVerificationUrl();
        OTPVerificationRequestBody requestBody = new OTPVerificationRequestBody();
        requestBody.setTxnId(txnId);
        requestBody.setOtp(otp);
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
                                Log.d("callOTPForMobileLoginVerificationApi", "onSuccess: " + mobileLoginOnOTPVerifiedResponse.toString());
                                if (mobileLoginOnOTPVerifiedResponse.getAccounts() != null) {
                                    if (mobileLoginOnOTPVerifiedResponse.getAccounts().size() > 0) {    // ie. there is atleast one (1) account.

                                        if (mobileLoginOnOTPVerifiedResponse.getAccounts().size() > 1) {
                                            // ie. there are more than 1 accounts for this mobile number than show -> Accounts selection screen.
                                            Intent intent = new Intent(context, AccountSelectionLoginActivity.class);
                                            intent.putExtra("payload", mobileLoginOnOTPVerifiedResponse);
                                            intent.putExtra("accessToken", accessToken);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            // ie. Only 1 account for this mobile number than call -> fetch User Profile details api.
                                            String ABHA_NUMBER = mobileLoginOnOTPVerifiedResponse.getAccounts().get(0).getABHANumber();
                                            String X_TOKEN = mobileLoginOnOTPVerifiedResponse.getToken();
                                            callFetchUserProfileAPI(ABHA_NUMBER, mobileLoginOnOTPVerifiedResponse.getTxnId(), X_TOKEN);
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.d("callOTPForMobileLoginVerificationApi", "onError: " + e.toString());
                            }
                        });
            }
        }).start();
    }

    /**
     * This will call the Fetch Profile Details api to fetch the details related to this user.
     * @param abhaNumber
     * @param txnId
     * @param xToken
     */
    private void callFetchUserProfileAPI(String abhaNumber, String txnId, String xToken) {
        
    }


    /**
     * Here, this function is used to call the EnrollByAadhar api which takes @BODY: txtId, mobileNo, otp and will return us
     * patient's details.
     * @param txnId
     * @param mobileNo
     * @param otp
     */
    private void callOTPForVerificationApi(String txnId, String mobileNo, String otp) {
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
                                // 1. if new user than isNew = true
                                // 2. if already exist user than isNew = false.
                                Log.d("callOTPForVerificationApi: ", "onSuccess: " + otpVerificationResponse.toString());

                                Intent intent;
                               /* if (otpVerificationResponse.getIsNew()) {
                                    // New user - than take to ABHA address screen.
                                    intent = new Intent(context, AbhaAddressSuggestionsActivity.class);
                                } else {
                                    // Already user exist - than take to Patient Registration screen.
                                    intent = new Intent(context, IdentificationActivity_New.class);
                                }*/ // todo: uncomment later.

                                intent = new Intent(context, AbhaAddressSuggestionsActivity.class); // todo: remove this later: testing...
                                intent.putExtra("payload", otpVerificationResponse);
                                intent.putExtra("accessToken", accessToken);
                                startActivity(intent);
                                finish();
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.d("callOTPForVerificationApi: ", "onError: " + e.toString());
                            }
                        });
                // api - end
            }
        }).start();

    }

    private boolean checkValidation() {
        if (hasABHA) {
            return areInputFieldsValid_HasABHA();
        }
        else {
            if (binding.layoutDoNotHaveABHANumber.aadharNoBox.getText().toString().isEmpty()) {
                binding.layoutDoNotHaveABHANumber.aadharNoBox.setError(getString(R.string.error_field_required));
                binding.layoutDoNotHaveABHANumber.aadharNoBox.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));
                return false;
            }
            if (binding.layoutDoNotHaveABHANumber.mobileNoBox.getText().toString().isEmpty()) {
                binding.layoutDoNotHaveABHANumber.mobileNoBox.setError(getString(R.string.error_field_required));
                binding.layoutDoNotHaveABHANumber.mobileNoBox.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));
                return false;
            }
            if (binding.layoutDoNotHaveABHANumber.aadharNoBox.getText().toString().length() < 12) {
                binding.layoutDoNotHaveABHANumber.aadharNoBox.setError("Enter 12 digits");
                binding.layoutDoNotHaveABHANumber.aadharNoBox.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));
                return false;
            }
            if (binding.layoutDoNotHaveABHANumber.mobileNoBox.getText().toString().length() < 10) {
                binding.layoutDoNotHaveABHANumber.mobileNoBox.setError(getString(R.string.enter_10_digits));
                binding.layoutDoNotHaveABHANumber.mobileNoBox.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));
                return false;
            }
        }

        // common area...
        if (binding.flOtpBox.getVisibility() == View.VISIBLE) {
            if (binding.otpBox.getText() != null) {
                if (binding.otpBox.getText().toString().isEmpty()) {
                    Toast.makeText(context, "Please enter OTP received!", Toast.LENGTH_LONG).show();
                    return false;
                }
            }
        }

        return true;
    }

    private boolean areInputFieldsValid_HasABHA() {
        if (!optionSelected.isEmpty() && optionSelected.equals("username")) {   // Aadhar field
            // aadhar validation - start
            String aadharNo = binding.layoutHaveABHANumber.edittextUsername.getText().toString().replace(" ","").trim();
            if (aadharNo.isEmpty()) {
                binding.layoutHaveABHANumber.edittextUsername.setError(getString(R.string.error_field_required));
                binding.layoutHaveABHANumber.edittextUsername.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));
                return false;
            }
            else if (aadharNo.length() != 12) {
                binding.layoutHaveABHANumber.edittextUsername.setError("Enter 12 digit");
                return false;
            }
            else {
                binding.layoutHaveABHANumber.edittextUsername.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_input_fieldnew));
               // return true;
            }
            // aadhar validaiton - end

            // mobile for aadhar - start
            String mobile = binding.layoutHaveABHANumber.edittextMobileNumber.getText().toString().replace(" ","").trim();
            Log.v(TAG, mobile);
            if (mobile.isEmpty()) {
                binding.layoutHaveABHANumber.edittextMobileNumber.setError(getString(R.string.error_field_required));
                binding.layoutHaveABHANumber.edittextMobileNumber.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));
                return false;
            } else if (/*code.equalsIgnoreCase("91") &&*/ mobile.length() != 10) {
                binding.layoutHaveABHANumber.edittextMobileNumber.setError(getString(R.string.enter_10_digits));
                //  tvMobileError.setText(getString(R.string.enter_10_digits));
                binding.layoutHaveABHANumber.edittextMobileNumber.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));
                return false;
            } else {
                binding.layoutHaveABHANumber.edittextMobileNumber.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_input_fieldnew));
                return true;
            }
            // mobile for aadhar - end

           /* if (!aadharNo.isEmpty() && aadharNo.length() == 12) {

            }*/
        }
        else if (!optionSelected.isEmpty() && optionSelected.equals("mobile")) {  // Phone number field
          //  String code = countryCodePicker.getSelectedCountryCode();
          //  mobile = mobile.replace(" ","");
          //  Log.v(TAG, code);
            String mobile = binding.layoutHaveABHANumber.edittextMobileNumber.getText().toString().replace(" ","").trim();
            Log.v(TAG, mobile);
            if (mobile.isEmpty()) {
                binding.layoutHaveABHANumber.edittextMobileNumber.setError(getString(R.string.error_field_required));
                binding.layoutHaveABHANumber.edittextMobileNumber.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));
                return false;
            } else if (/*code.equalsIgnoreCase("91") &&*/ mobile.length() != 10) {
                binding.layoutHaveABHANumber.edittextMobileNumber.setError(getString(R.string.enter_10_digits));
              //  tvMobileError.setText(getString(R.string.enter_10_digits));
                binding.layoutHaveABHANumber.edittextMobileNumber.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));
                return false;
            } else {
                binding.layoutHaveABHANumber.edittextMobileNumber.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_input_fieldnew));
                return true;
            }
        }

        return true;
    }

}