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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.github.ajalt.timberkt.Timber;

import org.intelehealth.app.R;
import org.intelehealth.app.abdm.dialog.MobileNumberOtpVerificationDialog;
import org.intelehealth.app.abdm.model.AadharApiBody;
import org.intelehealth.app.abdm.model.EnrollSuggestionRequestBody;
import org.intelehealth.app.abdm.model.EnrollSuggestionResponse;
import org.intelehealth.app.abdm.model.ExistUserStatusResponse;
import org.intelehealth.app.abdm.model.OTPResponse;
import org.intelehealth.app.abdm.model.OTPVerificationRequestBody;
import org.intelehealth.app.abdm.model.OTPVerificationResponse;
import org.intelehealth.app.abdm.model.TokenResponse;
import org.intelehealth.app.abdm.model.UpdateIdentifierReqBody;
import org.intelehealth.app.abdm.utils.ABDMConstant;
import org.intelehealth.app.abdm.utils.ABDMUtils;
import org.intelehealth.app.activities.identificationActivity.IdentificationActivity_New;
import org.intelehealth.app.activities.onboarding.PrivacyPolicyActivity_New;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.database.dao.PatientsDAO;
import org.intelehealth.app.databinding.ActivityCreateAbhaBinding;
import org.intelehealth.app.models.dto.PatientDTO;
import org.intelehealth.app.utilities.DialogUtils;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.SnackbarUtils;
import org.intelehealth.app.utilities.StringUtils;
import org.intelehealth.app.utilities.UrlModifiers;
import org.intelehealth.app.utilities.UuidDictionary;
import org.intelehealth.app.utilities.VerhoeffAlgorithm;
import org.intelehealth.app.utilities.WindowsUtils;
import org.intelehealth.app.widget.dialogs.ChecklistDialogFragment;
import org.intelehealth.app.widget.materialprogressbar.CustomProgressDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Response;


public class CreateAbhaAccountActivity extends AppCompatActivity {

    private final Context context = CreateAbhaAccountActivity.this;
    public static final String TAG = AadharMobileVerificationActivity.class.getSimpleName();
    ActivityCreateAbhaBinding binding;
    private String accessToken = "";
    public static final String BEARER_AUTH = "Bearer ";
    private CustomProgressDialog cpd;
    SnackbarUtils snackbarUtils;
    SessionManager sessionManager = null;
    private CountDownTimer countDownTimer;
    private int resendCounter = 2;
    private PatientDTO patientDTO = null;

    private String patientName;

    private ChecklistDialogFragment dialogFragment;
    private String mExistingPatientOpenMRSId = null;
    private String mExistingPatientUuid = null;

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateAbhaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        WindowsUtils.setStatusBarColor(CreateAbhaAccountActivity.this);  // changing status bar color
        cpd = new CustomProgressDialog(context);
        snackbarUtils = new SnackbarUtils();
        sessionManager = new SessionManager(context);
        patientDTO = (PatientDTO) getIntent().getSerializableExtra("patientDTO");

        binding.ivBackArrow.setOnClickListener(v -> finish());
        resendCounter = 2;

        // check internet - start
        checkInternetConnection();
        setClickListener();
        patientName = getIntent().getStringExtra(PrivacyPolicyActivity_New.intentPatientNameTag);
    }

    private void setClickListener() {
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
                if (binding.flOtpBox.getVisibility() != View.VISIBLE) {
                    binding.flOtpBox.setVisibility(View.VISIBLE);
                    binding.rlResendOTP.setVisibility(View.VISIBLE);
                    binding.llResendCounter.setVisibility(View.VISIBLE);
                    binding.tilMobile.setVisibility(View.VISIBLE);
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
                        mobileNo = Objects.requireNonNull(binding.mobileNoBox.getText()).toString().trim();
                        callOTPForAadhaarVerificationApi((String) binding.sendOtpBtn.getTag(), mobileNo, binding.otpBox.getText().toString());
                    }
                }
            }
        });

        binding.layoutOnlyaadhar.cvTermsAndCondition.setOnClickListener(v -> {

        });

        binding.layoutOnlyaadhar.cvTermsAndCondition.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        ConsentDialog consentDialog = new ConsentDialog(patientName);
                        consentDialog.setListeners(isCheck -> binding.layoutOnlyaadhar.cvTermsAndCondition.setChecked(isCheck));
                        consentDialog.show(getSupportFragmentManager(), ConsentDialog.class.getSimpleName());
                    }
                    binding.sendOtpBtn.setEnabled(isChecked);
                }
        );

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

    /**
     * This function is used to handle the resend counter and the necessary text to be displayed.
     */
    private void resendCounterAttemptsTextDisplay() {
        if (resendCounter != 0)
            binding.tvResendCounter.setText(getResources().getString(R.string.number_of_retries_left, resendCounter));
        else {
            binding.tvResendCounter.setText(getString(R.string.maximum_number_of_retries_exceeded_please_try_again_after_10_mins));
            disableUI(false);
            binding.resendBtn.setEnabled(false);
            binding.resendBtn.setTextColor(getColor(R.color.medium_gray));
            binding.resendBtn.setPaintFlags(binding.resendBtn.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            binding.resendBtn.setVisibility(View.GONE);
        }
    }


    private void callGenerateTokenApi() {   // Step 1.
        cpd.show(getString(R.string.otp_sending));
        disableUI(false);
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
                            disableUI(true);
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
        aadhaarNo = Objects.requireNonNull(binding.layoutOnlyaadhar.aadharNoBox.getText()).toString().trim();

        aadharApiBody.setScope(ABDMConstant.SCOPE_AADHAAR);
        aadharApiBody.setValue(aadhaarNo);
        String url = UrlModifiers.getAadharOTPVerificationUrl();

        Single<Response<OTPResponse>> responseBodySingle = AppConstants.apiInterface.GET_OTP_FOR_AADHAR(url, accessToken, aadharApiBody);
        new Thread(() -> {
            // api - start
            responseBodySingle.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new DisposableSingleObserver<>() {
                        @Override
                        public void onSuccess(Response<OTPResponse> response) {
                            initializeCountdownTimer();
                            cpd.dismiss();

                            if (response.code() == 200) {
                                OTPResponse otpResponse = response.body();
                                snackbarUtils.showSnackLinearLayoutParentSuccess(context, binding.layoutParent, StringUtils.getMessageTranslated(otpResponse.getMessage(), sessionManager.getAppLanguage()), true);

                                Timber.tag(TAG).d("onSuccess: AadhaarResponse: %s", otpResponse.toString());
                                // here, we will receive: txtID, otp
                                // and we need to pass to another api: otp, mobileNo and txtID will go in Header.

                                if (binding.flOtpBox.getVisibility() != View.VISIBLE) {
                                    binding.flOtpBox.setVisibility(View.VISIBLE);
                                    binding.rlResendOTP.setVisibility(View.VISIBLE);
                                    binding.llResendCounter.setVisibility(View.VISIBLE);
                                    binding.tilMobile.setVisibility(View.VISIBLE);
                                    binding.resendBtn.setPaintFlags(binding.resendBtn.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                                }

                                binding.sendOtpBtn.setTag(otpResponse.getTxnId());
                                binding.sendOtpBtn.setText(getString(R.string.verify));
                                binding.sendOtpBtn.setEnabled(true);    // btn enabled -> since otp is received.
                                disableUI(false);
                            } else if (response.code() == 429) {
                                snackbarUtils.showSnackLinearLayoutParentSuccess(context, binding.layoutParent, StringUtils.getMessageTranslated(getString(R.string.you_have_requested_multiple_otps_or_exceeded_maximum_number_of_attempts_for_otp_match_in_this_transaction_please_try_again_in_30_minutes), sessionManager.getAppLanguage()), false);
                                disableUI(false);
                                binding.sendOtpBtn.setEnabled(true);
                                binding.sendOtpBtn.setText(R.string.send_otp);  // Send otp.
                                binding.otpBox.setText("");

                                resendCounter = 0;
                                resendCounterAttemptsTextDisplay();
                            } else {
                                disableUI(false);
                                binding.sendOtpBtn.setEnabled(true);
                                binding.sendOtpBtn.setText(R.string.send_otp);  // Send otp.
                                binding.otpBox.setText("");
                                Toast.makeText(context, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                            }

                        }

                        @Override
                        public void onError(Throwable e) {
                            Timber.tag(TAG).e("onError: AadhaarResponse: %s", e.getMessage());
                            Toast.makeText(context, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                            disableUI(false);
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
        if (otp.length() < 6) {
            Toast.makeText(context, getString(R.string.please_enter_6_digit_valid_otp), Toast.LENGTH_SHORT).show();
            return;
        }
        cpd = new CustomProgressDialog(context);
        cpd.show(getString(R.string.verifying_otp));
        Timber.tag("callOTPForVerificationApi: ").d("parameters: " + txnId + ", " + mobileNo + ", " + otp);
        disableUI(false);
        binding.sendOtpBtn.setEnabled(false);    // btn disabled.


        // payload
        String url = UrlModifiers.getOTPForVerificationUrl();
        OTPVerificationRequestBody requestBody = new OTPVerificationRequestBody();
        requestBody.setOtp(otp);
        requestBody.setTxnId(txnId);
        requestBody.setMobileNo(mobileNo);

        Single<Response<OTPVerificationResponse>> otpVerificationResponseObservable =
                AppConstants.apiInterface.PUSH_OTP_FOR_VERIFICATION(url, accessToken, requestBody);

        new Thread(() -> {
            // api - start
            otpVerificationResponseObservable
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new DisposableSingleObserver<>() {
                        @Override
                        public void onSuccess(Response<OTPVerificationResponse> otpVerificationResponse) {
                            cpd.dismiss();
                            Timber.tag("callOTPForVerificationApi: ").d("onSuccess: %s", otpVerificationResponse.toString());
                            if (otpVerificationResponse.code() == 200) {
                                binding.sendOtpBtn.setTag(null);    // resetting...
                                OTPVerificationResponse otpResponse = otpVerificationResponse.body();
                                String mobile = otpResponse.getABHAProfile().getMobile();
                                boolean isMobileEmpty = TextUtils.isEmpty(mobile);
                                boolean isNewUser = otpResponse.getIsNew();

                                if (isMobileEmpty || !mobile.equalsIgnoreCase(mobileNo)) {
                                    MobileNumberOtpVerificationDialog mobileNumberOtpVerificationDialog = getMobileNumberOtpVerificationDialog(otpResponse);
                                    mobileNumberOtpVerificationDialog.show(getSupportFragmentManager(), "");
                                } else {
                                    handleUserFlow(otpResponse, accessToken);
                                }
                            } else if (otpVerificationResponse.code() == 400) {
                                Toast.makeText(context, getText(R.string.entered_aadhaar_or_mobile_number_is_incorrect), Toast.LENGTH_SHORT).show();
                                binding.sendOtpBtn.setEnabled(true);
                                disableUI(true);
                            } else if (otpVerificationResponse.code() == 422) {
                                Toast.makeText(context, getText(R.string.please_enter_valid_otp), Toast.LENGTH_SHORT).show();
                                binding.sendOtpBtn.setEnabled(true);
                                disableUI(false);
                            } else {
                                Toast.makeText(context, ABDMUtils.getErrorMessage(otpVerificationResponse), Toast.LENGTH_SHORT).show();
                                binding.sendOtpBtn.setEnabled(true);
                                disableUI(false);
                            }

                        }

                        private @NonNull MobileNumberOtpVerificationDialog getMobileNumberOtpVerificationDialog(OTPVerificationResponse otpResponse) {
                            MobileNumberOtpVerificationDialog mobileNumberOtpVerificationDialog = new MobileNumberOtpVerificationDialog();
                            mobileNumberOtpVerificationDialog.openMobileNumberVerificationDialog(accessToken, otpResponse.getTxnId(), mobileNo, onMobileEnrollCompleted -> {
                                mobileNumberOtpVerificationDialog.dismiss();
                                otpResponse.getABHAProfile().setMobile(mobileNo);
                                sessionManager.setIsCommunicationNumberUsed(true);
                                handleUserFlow(otpResponse, accessToken);
                            });
                            return mobileNumberOtpVerificationDialog;
                        }

                        @Override
                        public void onError(Throwable e) {
                            disableUI(false);
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

    private void handleUserFlow(OTPVerificationResponse otpVerificationResponse, String accessToken) {
        if (otpVerificationResponse.getABHAProfile().getPhrAddress() == null || otpVerificationResponse.getABHAProfile().getPhrAddress().isEmpty()) {
            callFetchAbhaAddressSuggestionsApi(otpVerificationResponse, accessToken);
        } else {
            checkIsUserExist(otpVerificationResponse.getABHAProfile().getABHANumber(), otpVerificationResponse);
        }
    }

    private void callFetchAbhaAddressSuggestionsApi(OTPVerificationResponse otpVerificationResponse, String accessToken) {
        if (cpd.isShowing()) {
            cpd.dismiss();
        }

        cpd.show();
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
                        cpd.dismiss();

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

        if (Objects.requireNonNull(binding.layoutOnlyaadhar.aadharNoBox.getText()).toString().isEmpty()) {
            binding.layoutOnlyaadhar.aadharError.setVisibility(View.VISIBLE);
            binding.layoutOnlyaadhar.aadharError.setText(getString(R.string.error_field_required));
            binding.layoutOnlyaadhar.aadharNoBox.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));
            isValid = false;
        } else { // ie. aadhaar no empty
            if (binding.layoutOnlyaadhar.aadharNoBox.getText().toString().length() < 12) {
                binding.layoutOnlyaadhar.aadharError.setVisibility(View.VISIBLE);
                binding.layoutOnlyaadhar.aadharError.setText(getString(R.string.enter_12_digits));
                binding.layoutOnlyaadhar.aadharNoBox.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));
                isValid = false;
            } else if (!validateAadhaarNumber(binding.layoutOnlyaadhar.aadharNoBox.getText().toString())) {
                binding.layoutOnlyaadhar.aadharError.setVisibility(View.VISIBLE);
                binding.layoutOnlyaadhar.aadharError.setText(R.string.aadhar_number_is_not_valid);
                binding.layoutOnlyaadhar.aadharNoBox.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));
                isValid = false;
            } else {
                binding.layoutOnlyaadhar.aadharError.setVisibility(View.GONE);
                binding.layoutOnlyaadhar.aadharNoBox.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_input_fieldnew));
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

            if (Objects.requireNonNull(binding.mobileNoBox.getText()).toString().isEmpty()) {
                binding.mobileError.setVisibility(View.VISIBLE);
                binding.mobileError.setText(getString(R.string.error_field_required));
                binding.mobileNoBox.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));
                isValid = false;
            } else {
                if (binding.mobileNoBox.getText().toString().length() < 10) {
                    binding.mobileError.setVisibility(View.VISIBLE);
                    binding.mobileError.setText(getString(R.string.enter_10_digits));
                    binding.mobileNoBox.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));
                    isValid = false;
                } else {
                    binding.mobileError.setVisibility(View.GONE);
                    binding.mobileNoBox.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_input_fieldnew));
                }
            }
        }

        return isValid;
    }

    private void checkIsUserExist(String abhaNumber, OTPVerificationResponse abhaProfileResponse) {
        sessionManager = new SessionManager(context);
        String encoded = sessionManager.getEncoded();
        String url = UrlModifiers.getCheckExistingUserUrl();
        cpd.show();
        // payload - end
        Single<ExistUserStatusResponse> abhaProfileResponseSingle = AppConstants.apiInterface.checkExistingUser(url + abhaNumber, "Basic " + encoded);
        abhaProfileResponseSingle
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSingleObserver<>() {
                    @Override
                    public void onSuccess(ExistUserStatusResponse response) {
                        cpd.dismiss();
                        if (response != null && response.getData() != null && response.getData().getUuid() != null) {
                            String openMrsId = response.getData().getUuid();
                            boolean doesUserExistOnHmis = !openMrsId.equalsIgnoreCase("NA");
                            if (doesUserExistOnHmis) {
                                mExistingPatientOpenMRSId = response.getData().getOpenmrsid();
                                mExistingPatientUuid = response.getData().getUuid();
                            }
                            showDialogForConfirmation(abhaProfileResponse);
                        } else {
                            navigateToIdentificationScreenForNewPatient(abhaProfileResponse);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        cpd.dismiss();
                    }
                });
    }

    private void showDialogForConfirmation(OTPVerificationResponse abhaProfileResponse) {
        ChecklistDialogFragment dialogFragment = getChecklistDialogFragment(abhaProfileResponse);
        this.dialogFragment = dialogFragment;
        dialogFragment.setCancelable(false);
        dialogFragment.show(getSupportFragmentManager(), ChecklistDialogFragment.TAG);
    }

    @NonNull
    private ChecklistDialogFragment getChecklistDialogFragment(OTPVerificationResponse abhaProfileResponse) {
        List<String> addressList = abhaProfileResponse.getABHAProfile().getPhrAddress();
        return new ChecklistDialogFragment(addressList, new DialogUtils.TextSelectedListener() {
            @Override
            public void onDialogActionDone(int action, String text) {
                if (action == DialogUtils.TextSelectedListener.POSITIVE_CLICK) {
                    addressList.remove(text);
                    addressList.add(0, text);
                    abhaProfileResponse.getABHAProfile().setPhrAddress(addressList);
                    if (mExistingPatientOpenMRSId != null && !mExistingPatientOpenMRSId.equals("NA")) {
                        boolean isExistingPatientWithSelectedAbhaAddress = new PatientsDAO().isPatientExistWithAbhaAddress(mExistingPatientOpenMRSId, text);
                        // call api to update identifier
                        if (isExistingPatientWithSelectedAbhaAddress) {
                            navigateToIdentificationScreenWithExistingDetails(abhaProfileResponse /*,response*/);
                        } else {
                            // add new identifier to existing patient
                            updatePatientIdentifier(abhaProfileResponse, text);
                        }
                    } else {
                        navigateToIdentificationScreenForNewPatient(abhaProfileResponse);
                    }
                    dialogFragment.dismiss();
                }
            }

            @Override
            public void onDialogActionDone(int action) {
                if (action == DialogUtils.TextSelectedListener.NEGATIVE_CLICK) {
                    if (addressList.size() >= 6) {
                        dialogFragment.displayError(getString(R.string.you_have_more_than_six_abha_addresses_error));
                        dialogFragment.shouldShowErrorMessage(true);
                    } else {
                        callFetchAbhaAddressSuggestionsApi(abhaProfileResponse, accessToken);
                        dialogFragment.dismiss();
                    }
                }
            }
        });
    }


    private void updatePatientIdentifier(OTPVerificationResponse abhaProfileResponse, String newAbhaAddress) {
        //{
        //"identifier":"rocketsingh@sbx",
        //"identifierType":"59077d8f-8bee-4a6f-a1a8-64365a297da6",
        //"location":"uuid-for-location"
        //}
        UpdateIdentifierReqBody requestBody = new UpdateIdentifierReqBody();
        requestBody.setIdentifier(newAbhaAddress);
        requestBody.setIdentifierType(UuidDictionary.UPDATE_IDENTIFIER_TYPE_UUID);
        requestBody.setLocation(sessionManager.getLocationUuid());

        String url = UrlModifiers.getUpdatePatientIdentifierUrl(mExistingPatientUuid);
        cpd.show();
        // post method
        Single<Response<ResponseBody>> responseSingle = AppConstants.apiInterface.updatePatientIdentifier(
                url,
                "Basic " + sessionManager.getEncoded(),
                requestBody
        );

        responseSingle.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSingleObserver<>() {
                    @Override
                    public void onSuccess(Response<ResponseBody> response) {
                        cpd.dismiss();
                        if (response.code() == 200 || response.code() == 204) {
                            Timber.tag(TAG).d("onSuccess: update identifier success");
                        } else {
                            Timber.tag(TAG).d("onSuccess: update identifier failed with code %s", response.code());
                        }
                        navigateToIdentificationScreenWithExistingDetails(abhaProfileResponse /*,response*/);
                    }

                    @Override
                    public void onError(Throwable e) {
                        cpd.dismiss();
                        Timber.tag(TAG).e("onError: update identifier error %s", e.toString());
                    }
                });
    }

    private void navigateToIdentificationScreenWithExistingDetails(OTPVerificationResponse abhaProfileResponse/*, ExistUserStatusResponse response*/) {
        abhaProfileResponse.setOpenMrsId(mExistingPatientOpenMRSId);
        abhaProfileResponse.setUuID(mExistingPatientUuid);
        Intent intent = new Intent(context, IdentificationActivity_New.class);
        intent.putExtra(PAYLOAD, abhaProfileResponse);
        intent.putExtra("accessToken", accessToken);
        intent.putExtra("patient_detail", true);
        intent.putExtra("firstRequestFulfilled", true);
        startActivity(intent);
        finish();
    }

    private void navigateToIdentificationScreenForNewPatient(OTPVerificationResponse abhaProfileResponse) {
        Intent intent = new Intent(context, IdentificationActivity_New.class);
        intent.putExtra(PAYLOAD, abhaProfileResponse);
        intent.putExtra("accessToken", accessToken);
        startActivity(intent);
        finish();
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
        disableUI(false);

        binding.resendBtn.setEnabled(false);
        binding.resendBtn.setTextColor(getColor(R.color.medium_gray));
        binding.sendOtpBtn.setText(R.string.send_otp);  // Send otp.
    }

    private void initializeCountdownTimer() {
        String resendTime = getResources().getString(R.string.resend_otp_in);

        if (countDownTimer != null) {
            countDownTimer.cancel();    // reset any existing countdown.
        }

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
                    disableUI(true);
                    binding.resendBtn.setEnabled(true);
                    binding.resendBtn.setTextColor(getColor(R.color.colorPrimary));
                }

                binding.resendBtn.setText(getResources().getString(R.string.resend_otp));
                if (cpd != null && cpd.isShowing())
                    cpd.dismiss();
            }
        }.start();
    }

    private void disableUI(boolean shouldEnable) {
        binding.layoutOnlyaadhar.aadharNoBox.setEnabled(shouldEnable);
        binding.layoutOnlyaadhar.cvTermsAndCondition.setEnabled(shouldEnable);
    }
}