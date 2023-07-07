package org.intelehealth.ezazi.ui.password.fragment;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding = FragmentOtpVerificationBinding.bind(view);
        pinEntryEditText = binding.contentOtpVerification.pinEntryEditText;
        customProgressDialog = new CustomProgressDialog(requireActivity());
        mContext = requireActivity();

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
                    binding.contentOtpVerification.tvErrorPin.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    private void verifyOTP() {
        PasswordViewModel viewModel = new ViewModelProvider(
                requireActivity(), ViewModelProvider.Factory.from(PasswordViewModel.initializer)
        ).get(PasswordViewModel.class);

        RequestOTPModel requestOTPModel = viewModel.requestOtpModel.getValue();
        VerifyOtpRequestModel requestModel = new VerifyOtpRequestModel();
        if (requestOTPModel != null) {
            requestModel.setOtpFor(ForgotPasswordFragment.OTPForString);
            requestModel.setCountryCode(requestOTPModel.getCountryCode());
            requestModel.setPhoneNumber(requestOTPModel.getPhoneNumber());
            requestModel.setOtp(binding.contentOtpVerification.pinEntryEditText.getText().toString());
            viewModel.verifyOtp(requestModel);
        }

        observeData(viewModel);

    }

    private void observeData(PasswordViewModel viewModel) {
        //success
        viewModel.verifyOtpData.observe(requireActivity(), verifyOtpResultData -> {
            if (verifyOtpResultData.getUserUuid() != null) {
                Toast.makeText(mContext, getResources().getString(R.string.otp_verified), Toast.LENGTH_SHORT).show();
                Navigation.findNavController(requireView()).navigate(org.intelehealth.ezazi.ui.password.fragment.OTPVerificationFragmentDirections.otpVerificationToResetPasswordFragment());
            }
        });

        //observe loading - progress dialog
        viewModel.loading.observe(requireActivity(), aBoolean -> {
            if (aBoolean) {
                customProgressDialog.show();
            } else {
                if (customProgressDialog.isShowing()) {
                    customProgressDialog.dismiss();
                }
            }
        });

        //failure - success - false
        viewModel.otpVerifyFailureResult.observe(requireActivity(), failureResultData -> {
            Toast.makeText(mContext, failureResultData, Toast.LENGTH_SHORT).show();
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

}
