package org.intelehealth.ezazi.ui.password.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavArgs;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import com.alimuzaffar.lib.pin.PinEntryEditText;

import org.intelehealth.ezazi.R;
import org.intelehealth.ezazi.databinding.FragmentOtpVerificationBinding;
import org.intelehealth.ezazi.ui.password.model.RequestOTPModel;
import org.intelehealth.ezazi.ui.password.model.VerifyOtpRequestModel;
import org.intelehealth.ezazi.ui.password.viewmodel.PasswordViewModel;
import org.intelehealth.ezazi.widget.materialprogressbar.CustomProgressDialog;

/**
 * Created by Vaghela Mithun R. on 26-05-2023 - 11:19.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public class OTPVerificationFragment extends Fragment {
    private static final String TAG = "OTPVerificationFragment";
    private FragmentOtpVerificationBinding binding;
    private CustomProgressDialog customProgressDialog;

    public OTPVerificationFragment() {
        super(R.layout.fragment_otp_verification);
    }

    private PinEntryEditText pinEntryEditText;
    private Context mContext;
    private TextView tvResendOtp;
    private PasswordViewModel viewModel;
    private OTPVerificationFragmentArgs requiredArgs;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding = FragmentOtpVerificationBinding.bind(view);
        pinEntryEditText = binding.contentOtpVerification.pinEntryEditText;
        customProgressDialog = new CustomProgressDialog(requireActivity());
        mContext = requireActivity();
        tvResendOtp = binding.contentOtpVerification.tvResend;

        getRequiredArguments();
        resendOtpTimer();

        viewModel = new ViewModelProvider(
                this, ViewModelProvider.Factory.from(PasswordViewModel.initializer)
        ).get(PasswordViewModel.class);


        handleClickListeners();
        observeData(viewModel);


    }

    private void handleClickListeners() {
        binding.btnContinue.setOnClickListener(view1 -> {
            if (areValidFields())
                verifyOTP();
        });

        pinEntryEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 6) {
                    binding.btnContinue.setEnabled(true);
                    binding.contentOtpVerification.tvErrorPin.setVisibility(View.GONE);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    binding.btnContinue.setEnabled(true);
                }
            }
        });
        tvResendOtp.setOnClickListener(v -> resendOtpApiCall());
    }

    private void getRequiredArguments() {
        requiredArgs = OTPVerificationFragmentArgs.fromBundle(getArguments());
    }

    private void verifyOTP() {
        RequestOTPModel requestOTPModel = requiredArgs.getRequestOtpModel();
        VerifyOtpRequestModel requestModel = new VerifyOtpRequestModel();
        if (requestOTPModel != null) {
            requestModel.setOtpFor(ForgotPasswordFragment.OTPForString);
            requestModel.setCountryCode(requestOTPModel.getCountryCode());
            requestModel.setPhoneNumber(requestOTPModel.getPhoneNumber());
            requestModel.setOtp(binding.contentOtpVerification.pinEntryEditText.getText().toString());
            viewModel.verifyOtp(requestModel);
        }


    }

    private void observeData(PasswordViewModel viewModel) {
        //success
        viewModel.verifyOtpData.observe(getViewLifecycleOwner(), verifyOtpResultData -> {
            if (verifyOtpResultData != null && verifyOtpResultData.getUserUuid() != null) {
                binding.contentOtpVerification.tvErrorPin.setVisibility(View.GONE);
                Toast.makeText(mContext, getResources().getString(R.string.otp_verified), Toast.LENGTH_SHORT).show();
                // NavDirections navDir = OTPVerificationFragmentDirections.otpVerificationToResetPasswordFragment();
                if (Navigation.findNavController(requireView()).getCurrentDestination().getId() == R.id.fragmentOtpVerification)
                    Navigation.findNavController(requireView()).navigate(OTPVerificationFragmentDirections.otpVerificationToResetPasswordFragment(verifyOtpResultData.getUserUuid()));
            }
        });
        //observe loading - progress dialog
        viewModel.loading.observe(getViewLifecycleOwner(), aBoolean -> {
            if (aBoolean) {
                customProgressDialog.show();
            } else {
                if (customProgressDialog.isShowing()) {
                    customProgressDialog.dismiss();
                }
            }
        });

        //failure - success - false
        viewModel.failDataResult.observe(getViewLifecycleOwner(), failureResultData -> {
            pinEntryEditText.setText("");
            Toast.makeText(mContext, failureResultData, Toast.LENGTH_SHORT).show();

        });

        //api failure
        viewModel.errorDataResult.observe(getViewLifecycleOwner(), errorResult -> {
        });
    }

    private boolean areValidFields() {
        boolean result = false;
        if (TextUtils.isEmpty(pinEntryEditText.getText().toString()) || pinEntryEditText.length() != 6) {
            binding.contentOtpVerification.tvErrorPin.setVisibility(View.VISIBLE);
            binding.contentOtpVerification.tvErrorPin.setText(getResources().getString(R.string.please_enter_pin));
            // binding.contentOtpVerification.pinEntryEditText.setPinBackground(ContextCompat.getDrawable(requireActivity(),R.drawable.selectot_pin_error));
        } else {
            binding.contentOtpVerification.tvErrorPin.setVisibility(View.GONE);
            result = true;
        }
        return result;
    }

    private void resendOtpTimer() {
        if (mContext != null) {
            pinEntryEditText.setText("");
            tvResendOtp.setEnabled(false);
            String resendTime = getResources().getString(R.string.resend_otp_in);
            new CountDownTimer(60000, 1000) {

                public void onTick(long millisUntilFinished) {
                    String time = resendTime + " " + millisUntilFinished / 1000 + " " + mContext.getResources().getString(R.string.seconds);
                    tvResendOtp.setText(time);
                }

                public void onFinish() {
                    tvResendOtp.setEnabled(true);
                    tvResendOtp.setText(mContext.getResources().getString(R.string.lbl_resend));
                }

            }.start();
        }
    }

    private void resendOtpApiCall() {
        binding.contentOtpVerification.tvErrorPin.setVisibility(View.GONE);

        RequestOTPModel observedOtpDataModel = requiredArgs.getRequestOtpModel();

        if (observedOtpDataModel != null) {
            RequestOTPModel requestOTPModel = new RequestOTPModel(ForgotPasswordFragment.OTPForString, observedOtpDataModel.getPhoneNumber(), observedOtpDataModel.getCountryCode());
            viewModel.requestOtp(requestOTPModel);
        }
        viewModel.requestOTPResponseData.observe(getViewLifecycleOwner(), requestOTPResult -> {
            if (requestOTPResult.getUserUuid() != null) {
                Toast.makeText(mContext, getResources().getString(R.string.otp_sent), Toast.LENGTH_SHORT).show();
            }
        });

        //failure - success - false
        viewModel.failDataResult.observe(getViewLifecycleOwner(), failureResultData -> {
            pinEntryEditText.setText("");
          /*  binding.contentOtpVerification.tvErrorPin.setVisibility(View.VISIBLE);
            binding.contentOtpVerification.tvErrorPin.setText(failureResultData);*/
            Toast.makeText(mContext, failureResultData, Toast.LENGTH_SHORT).show();
        });

        //api failure
        viewModel.errorDataResult.observe(getViewLifecycleOwner(), errorResult -> {
        });
    }

}
