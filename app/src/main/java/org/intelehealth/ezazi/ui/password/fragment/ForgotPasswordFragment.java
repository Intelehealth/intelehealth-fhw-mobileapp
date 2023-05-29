package org.intelehealth.ezazi.ui.password.fragment;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import org.intelehealth.ezazi.R;
import org.intelehealth.ezazi.databinding.FragmentForgotPasswordBinding;

/**
 * Created by Vaghela Mithun R. on 26-05-2023 - 11:19.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public class ForgotPasswordFragment extends Fragment {
    private FragmentForgotPasswordBinding binding;

    public ForgotPasswordFragment() {
        super(R.layout.fragment_forgot_password);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding = FragmentForgotPasswordBinding.bind(view);
        binding.btnContinue.setOnClickListener(view1 -> {
            Navigation.findNavController(requireView()).navigate(ForgotPasswordFragmentDirections.forgotToOtpVerificationFragment());
        });
    }
}
