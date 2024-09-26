package org.intelehealth.abdm.features.ui.registration

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import org.intelehealth.abdm.R
import org.intelehealth.abdm.databinding.ActivityAadhaarVerificationBinding
import org.intelehealth.abdm.databinding.FragmentAadhaarCardVerificationBinding
import org.intelehealth.abdm.databinding.FragmentAadhaarOtpVerificationBinding
import org.intelehealth.abdm.features.base.BaseFragment
import org.intelehealth.abdm.features.intent.RegistrationVerificationIntent
import org.intelehealth.abdm.features.viewmodel.registration.AadhaarCardVerificationViewModel

class AadhaarOtpVerificationFragment :
    BaseFragment<FragmentAadhaarOtpVerificationBinding, AadhaarCardVerificationViewModel>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnVerifyOtp.btnActive.text = getString(R.string.verify_otp)
        setClickListener()
    }

    override fun setClickListener() {
        binding.btnVerifyOtp.btnActive.setOnClickListener {
            viewModel.sendIntent(RegistrationVerificationIntent.OnClickVerifyAadhaarOtp(binding.otpBox.text.toString()))
        }
    }

    override fun initViewModel() = ViewModelProvider(
        requireActivity()
    )[AadhaarCardVerificationViewModel::class.java]

    override fun initBinding(): FragmentAadhaarOtpVerificationBinding =
        FragmentAadhaarOtpVerificationBinding.inflate(layoutInflater)
}