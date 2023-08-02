package org.intelehealth.ezazi.ui.password.fragment;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;

import org.intelehealth.ezazi.R;
import org.intelehealth.ezazi.databinding.FragmentResetPasswordBinding;
import org.intelehealth.ezazi.ui.shared.InputChangeValidationListener;
import org.intelehealth.ezazi.ui.dialog.ConfirmationDialogFragment;
import org.intelehealth.ezazi.ui.password.model.ChangePasswordRequestModel;
import org.intelehealth.ezazi.ui.password.viewmodel.PasswordViewModel;
import org.intelehealth.ezazi.utilities.SessionManager;
import org.intelehealth.ezazi.widget.materialprogressbar.CustomProgressDialog;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    String userUuid = "";
    SessionManager sessionManager;
    PasswordViewModel viewModel;
    boolean isNewPasswordValid;
    boolean isConfirmPasswordValid;

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
        sessionManager = new SessionManager(mContext);
        userUuid = ResetPasswordFragmentArgs.fromBundle(getArguments()).getUserUuid();

        viewModel = new ViewModelProvider(this, ViewModelProvider.Factory.from(PasswordViewModel.initializer)).get(PasswordViewModel.class);
        observeData();

        binding.btnSave.setOnClickListener(view1 -> {
            if (areValidFields()) resetPassword();
        });

        addValidationListener();


        setupScreenBack();
    }

    private void setupScreenBack() {
        requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                ConfirmationDialogFragment dialog = new ConfirmationDialogFragment.Builder(requireContext()).content(getString(R.string.are_you_want_go_back)).positiveButtonLabel(R.string.yes).build();

                dialog.setListener(() -> requireActivity().finish());

                dialog.show(getChildFragmentManager(), dialog.getClass().getCanonicalName());
            }
        });

    }

    private void resetPassword() {
        if (!userUuid.isEmpty()) {
            viewModel.resetPassword(userUuid, new ChangePasswordRequestModel(mNewPassword.getText().toString()));
        }
    }

    private void observeData() {
        viewModel.changePasswordResponse.observe(getViewLifecycleOwner(), changePasswordResultData -> {
            Toast.makeText(mContext, getResources().getString(R.string.password_reset_success), Toast.LENGTH_SHORT).show();
            requireActivity().finish();
//            navigateToNextActivity();
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

        viewModel.failDataResult.observe(getViewLifecycleOwner(), failureResultData -> {
            Toast.makeText(mContext, failureResultData, Toast.LENGTH_SHORT).show();
        });
        //api failure
        viewModel.errorDataResult.observe(getViewLifecycleOwner(), errorResult -> {
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

    private void addValidationListener() {

        new InputChangeValidationListener(binding.contentResetPassword.etNewPasswordLayout, new InputChangeValidationListener.InputValidator() {
            @Override
            public boolean validate(String text) {
                return isValidPassword(text);
            }

            @Override
            public void onValidatted(boolean isValid) {
                // binding.btnSave.setEnabled(isValid);
                isNewPasswordValid = isValid;
                binding.btnSave.setEnabled(isNewPasswordValid && isConfirmPasswordValid);
            }
        }).validate(getString(R.string.error_invalid_pwd));

        new InputChangeValidationListener(binding.contentResetPassword.etConfirmPasswordLayout, new InputChangeValidationListener.InputValidator() {
            @Override
            public boolean validate(String text) {
                return isValidConfirmPassword();
            }

            @Override
            public void onValidatted(boolean isValid) {
                //binding.btnSave.setEnabled(isValid);
                isConfirmPasswordValid = isValid;
                binding.btnSave.setEnabled(isNewPasswordValid && isConfirmPasswordValid);
            }
        }).validate(getString(R.string.password_not_matching));
    }

    private boolean isValidConfirmPassword() {
        String newPwd = Objects.requireNonNull(binding.contentResetPassword.etNewPassword.getText()).toString();
        String confPwd = Objects.requireNonNull(binding.contentResetPassword.etConfirmPassword.getText()).toString();
        return newPwd.equals(confPwd);
    }

    public boolean isValidPassword(final String password) {
        Pattern pattern;
        Matcher matcher;

        final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@*#$%^&+=])(?=\\S+$).{8,}$";

        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(password);

        return matcher.matches();

    }
}
