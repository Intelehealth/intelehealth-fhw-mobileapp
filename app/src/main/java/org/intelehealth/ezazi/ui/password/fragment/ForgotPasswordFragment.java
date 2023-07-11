package org.intelehealth.ezazi.ui.password.fragment;

import android.content.Context;
import android.graphics.Path;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import com.google.android.material.textfield.TextInputEditText;
import com.hbb20.CountryCodePicker;

import org.intelehealth.ezazi.R;
import org.intelehealth.ezazi.databinding.FragmentForgotPasswordBinding;
import org.intelehealth.ezazi.ui.InputChangeValidationListener;
import org.intelehealth.ezazi.ui.password.model.RequestOTPModel;
import org.intelehealth.ezazi.ui.password.viewmodel.PasswordViewModel;
import org.intelehealth.ezazi.widget.materialprogressbar.CustomProgressDialog;

/**
 * Created by Vaghela Mithun R. on 26-05-2023 - 11:19.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public class ForgotPasswordFragment extends Fragment {
    private static final String TAG = "ForgotPasswordFragment";
    private FragmentForgotPasswordBinding binding;
    private String mSelectedCountryCode = "";
    private CountryCodePicker mCountryCodePicker;
    private TextInputEditText mPhoneNumberEditText;
    private int mSelectedMobileNumberValidationLength = 0;
    public static final String OTPForString = "password";
    private CustomProgressDialog customProgressDialog;
    private Context mContext;
    private RequestOTPModel requestOTPModel;
    PasswordViewModel viewModel;

    public ForgotPasswordFragment() {
        super(R.layout.fragment_forgot_password);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding = FragmentForgotPasswordBinding.bind(view);
        customProgressDialog = new CustomProgressDialog(requireActivity());
        mContext = requireActivity();

        viewModel = new ViewModelProvider(this, ViewModelProvider.Factory.from(PasswordViewModel.initializer)).get(PasswordViewModel.class);

        binding.btnContinue.setOnClickListener(view1 -> {
            if (areValidFields()) {
                requestOTP();
            }
        });

        mCountryCodePicker = binding.contentForgotPassword.countrycodeSpinner;
        mPhoneNumberEditText = binding.contentForgotPassword.etEmail;

        mCountryCodePicker.registerCarrierNumberEditText(mPhoneNumberEditText);
        mCountryCodePicker.setNumberAutoFormattingEnabled(false);
        mCountryCodePicker.showNameCode(false);
        setMobileNumberLimit();
        addValidationListener();
        observeData();
    }


    private void setMobileNumberLimit() {
        mSelectedCountryCode = mCountryCodePicker.getSelectedCountryCode();
        if (mSelectedCountryCode.equals("91")) {
            mSelectedMobileNumberValidationLength = 10;
        }
        mPhoneNumberEditText.setInputType(InputType.TYPE_CLASS_PHONE);
        InputFilter inputFilter = (source, start, end, dest, dstart, dend) -> null;
        mPhoneNumberEditText.setFilters(new InputFilter[]{inputFilter, new InputFilter.LengthFilter(mSelectedMobileNumberValidationLength)});
    }

    private void requestOTP() {
        PasswordViewModel viewModel = new ViewModelProvider(this, ViewModelProvider.Factory.from(PasswordViewModel.initializer)).get(PasswordViewModel.class);

        requestOTPModel = new RequestOTPModel(OTPForString, mPhoneNumberEditText.getText().toString(), mSelectedCountryCode);
        viewModel.requestOtp(requestOTPModel);

    }

    private void observeData() {
        //success
        viewModel.requestOTPResponseData.observe(getViewLifecycleOwner(), requestOTPResult -> {
            Log.d(TAG, "observeData: ");
            if (requestOTPResult != null && requestOTPResult.getUserUuid() != null) {
                Toast.makeText(mContext, getResources().getString(R.string.otp_sent), Toast.LENGTH_SHORT).show();

                NavDirections directions = ForgotPasswordFragmentDirections.forgotToOtpVerificationFragment(requestOTPModel);
                Navigation.findNavController(requireView()).navigate(directions);
                viewModel.clearPreviousResult();
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
            //Toast.makeText(mContext, failureResultData, Toast.LENGTH_SHORT).show();
            if (failureResultData.toLowerCase().contains("no")) {
                binding.contentForgotPassword.etUsernameLayout.setFocusable(true);
                binding.contentForgotPassword.etUsernameLayout.setError(getString(R.string.no_user_exist));
            }
        });

        //api failure
        viewModel.errorDataResult.observe(getViewLifecycleOwner(), errorResult -> {
        });
    }

    private boolean areValidFields() {
        boolean result = false;

        if (TextUtils.isEmpty(mCountryCodePicker.getSelectedCountryCode())) {
            //binding.contentForgotPassword.etUsernameLayout.setError("Please select country");
        } else if (TextUtils.isEmpty(mPhoneNumberEditText.getText().toString())) {
            binding.contentForgotPassword.etUsernameLayout.setError(getString(R.string.enter_mobile_number));
        } else if (!isPhoneNumberValid(mPhoneNumberEditText.getText().toString())) {
            binding.contentForgotPassword.etUsernameLayout.setFocusable(true);
            binding.contentForgotPassword.etUsernameLayout.setError(getString(R.string.mobile_no_length));
        } else {
            result = true;
        }
        return result;
    }

    private void addValidationListener() {
        new InputChangeValidationListener(binding.contentForgotPassword.etUsernameLayout, new InputChangeValidationListener.InputValidator() {
            @Override
            public boolean validate(String text) {
                return isPhoneNumberValid(text);
            }

            @Override
            public void onValidatted(boolean isValid) {
                binding.btnContinue.setEnabled(isValid);
            }
        }).validate(getString(R.string.mobile_no_length));
    }

    private boolean isPhoneNumberValid(String phoneNo) {
        return phoneNo.length() == 10;
    }
}
