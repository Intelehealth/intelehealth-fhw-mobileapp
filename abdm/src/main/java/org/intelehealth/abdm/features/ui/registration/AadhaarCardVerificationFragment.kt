package org.intelehealth.abdm.features.ui.registration

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import org.intelehealth.abdm.R
import org.intelehealth.abdm.databinding.FragmentAadhaarCardVerificationBinding
import org.intelehealth.abdm.features.base.BaseFragment
import org.intelehealth.abdm.features.intent.RegistrationVerificationIntent
import org.intelehealth.abdm.features.viewmodel.registration.AadhaarCardVerificationViewModel

class AadhaarCardVerificationFragment :
    BaseFragment<FragmentAadhaarCardVerificationBinding, AadhaarCardVerificationViewModel>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnSendOtp.btnActive.text = getString(R.string.send_otp)
        setClickListener()
    }

    override fun setClickListener() {
        binding.btnSendOtp.btnActive.setOnClickListener {
            viewModel.sendIntent(
                RegistrationVerificationIntent.OnClickSendOtp(
                    binding.etAadhaarNo.text.toString(),
                    binding.etPhoneNo.text.toString()
                )
            )
        }
    }

    override fun initViewModel(): AadhaarCardVerificationViewModel = ViewModelProvider(
        requireActivity()
    )[AadhaarCardVerificationViewModel::class.java]

    override fun initBinding() = FragmentAadhaarCardVerificationBinding.inflate(layoutInflater)
}