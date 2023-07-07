package org.intelehealth.ezazi.ui.password.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;

import org.intelehealth.ezazi.R;
import org.intelehealth.ezazi.activities.loginActivity.LoginActivity;
import org.intelehealth.ezazi.activities.setupActivity.SetupActivity;
import org.intelehealth.ezazi.databinding.FragmentResetPasswordBinding;
import org.intelehealth.ezazi.ui.InputChangeValidationListener;
import org.intelehealth.ezazi.ui.password.model.ChangePasswordRequestModel;
import org.intelehealth.ezazi.ui.password.viewmodel.PasswordViewModel;
import org.intelehealth.ezazi.widget.materialprogressbar.CustomProgressDialog;

/**
 * Created by Kaveri Zaware on 05-07-2023
 * email - kaveri@intelehealth.org
 **/
public class ResetPasswordFragment extends Fragment {
    private static final String TAG = "ResetPasswordFragment";
    private FragmentResetPasswordBinding binding;
    private TextInputEditText mNewPassword, mConfirmPassword;
    private View focusView = null;
    private CustomProgressDialog customProgressDialog;
    private Context mContext;

    public ResetPasswordFragment() {
        super(R.layout.fragment_reset_password);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding = FragmentResetPasswordBinding.bind(view);
        customProgressDialog = new CustomProgressDialog(requireActivity());
        mContext = requireActivity();
        mNewPassword = binding.contentResetPassword.etNewPassword;
        mConfirmPassword = binding.contentResetPassword.etConfirmPassword;

        binding.btnSave.setOnClickListener(view1 -> {
            if (areValidFields())
                resetPassword();
        });

        addValidationListener();
    }

    private void resetPassword() {
        PasswordViewModel viewModel = new ViewModelProvider(requireActivity(), ViewModelProvider.Factory.from(PasswordViewModel.initializer)).get(PasswordViewModel.class);

        viewModel.requestOTPResponseData.observe(requireActivity(), verifyOtpResultData -> {
            if (verifyOtpResultData.getUserUuid() != null) {
                viewModel.resetPassword(verifyOtpResultData.getUserUuid(), new ChangePasswordRequestModel(mNewPassword.getText().toString()));

            }
        });

        observeData(viewModel);

    }

    private void observeData(PasswordViewModel viewModel) {
        viewModel.changePasswordResponse.observe(requireActivity(), changePasswordResultData -> {
            Toast.makeText(mContext, getResources().getString(R.string.password_reset_success), Toast.LENGTH_SHORT).show();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //navigate to login screen
                    Intent intent = new Intent(requireActivity(), SetupActivity.class);
                    startActivity(intent);
                }
            }, 1500);

        });
        //observe loading
        viewModel.loading.observe(getViewLifecycleOwner(), aBoolean -> {
            if (aBoolean) {
                customProgressDialog.show();
            } else {
                if (customProgressDialog.isShowing()) {
                    customProgressDialog.dismiss();
                }
            }
        });

        viewModel.resetPasswordFailureResult.observe(requireActivity(), failureResultData -> {
            Toast.makeText(mContext, failureResultData, Toast.LENGTH_SHORT).show();
        });
    }

    private boolean areValidFields() {
        boolean result = false;

        if (TextUtils.isEmpty(mNewPassword.getText().toString())) {
            binding.contentResetPassword.etNewPasswordLayout.setError(getString(R.string.enter_new_password));
            focusView = mNewPassword;
        } else if (mNewPassword.getText().toString().length() < 7) {
            binding.contentResetPassword.etNewPasswordLayout.setError(getString(R.string.error_invalid_password));
            focusView = mNewPassword;
        } else if (TextUtils.isEmpty(mConfirmPassword.getText().toString())) {
            binding.contentResetPassword.etConfirmPasswordLayout.setError(getString(R.string.reenter_password));
            focusView = mConfirmPassword;
        } else if (mConfirmPassword.getText().toString().length() < 7) {
            binding.contentResetPassword.etConfirmPasswordLayout.setError(getString(R.string.error_invalid_password));
            focusView = mConfirmPassword;
        } else if (!mNewPassword.getText().toString().equals(mConfirmPassword.getText().toString())) {
            binding.contentResetPassword.etConfirmPasswordLayout.setError(getString(R.string.password_not_matching));
            focusView = mConfirmPassword;
        } else {
            result = true;
        }
        return result;
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 7;
    }

    private boolean isConfirmPasswordValid(String password) {
        return password.length() > 7;
    }

    private void addValidationListener() {
        new InputChangeValidationListener(binding.contentResetPassword.etNewPasswordLayout, this::isPasswordValid)
                .validate(getString(R.string.error_invalid_password));
        new InputChangeValidationListener(binding.contentResetPassword.etConfirmPasswordLayout, this::isConfirmPasswordValid)
                .validate(getString(R.string.error_invalid_password));
    }
}
