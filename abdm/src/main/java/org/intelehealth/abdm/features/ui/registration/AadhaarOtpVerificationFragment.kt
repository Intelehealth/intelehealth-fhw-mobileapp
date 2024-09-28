package org.intelehealth.abdm.features.ui.registration

import android.os.Bundle
import android.view.View
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import org.intelehealth.abdm.R
import org.intelehealth.abdm.databinding.FragmentAadhaarOtpVerificationBinding
import org.intelehealth.abdm.features.base.BaseFragment
import org.intelehealth.abdm.features.intent.RegistrationVerificationIntent
import org.intelehealth.abdm.features.viewmodel.registration.AadhaarCardVerificationViewModel

class AadhaarOtpVerificationFragment :
    BaseFragment<FragmentAadhaarOtpVerificationBinding, AadhaarCardVerificationViewModel>() {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnVerifyOtp.btnActive.text = getString(R.string.verify_otp)
        binding.btnVerifyOtp.btnActive.isEnabled = false
        setClickListener()
    }

    override fun setClickListener() {
        binding.btnVerifyOtp.btnActive.setOnClickListener {
            viewModel.sendIntent(RegistrationVerificationIntent.OnClickVerifyAadhaarOtp(binding.otpBox.text.toString()))
        }
        binding.otpBox.doAfterTextChanged {
            binding.btnVerifyOtp.btnActive.isEnabled =
                !(binding.otpBox.text.isNullOrBlank() || binding.otpBox.text.toString().length < 6)
        }
    }

    override fun initViewModel() = ViewModelProvider(
        requireActivity()
    )[AadhaarCardVerificationViewModel::class.java]

    override fun initBinding(): FragmentAadhaarOtpVerificationBinding =
        FragmentAadhaarOtpVerificationBinding.inflate(layoutInflater)

}