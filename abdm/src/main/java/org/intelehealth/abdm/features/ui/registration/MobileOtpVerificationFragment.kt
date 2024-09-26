package org.intelehealth.abdm.features.ui.registration

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import org.intelehealth.abdm.R
import org.intelehealth.abdm.common.utils.ProgressBarUtils
import org.intelehealth.abdm.common.utils.ToastUtil
import org.intelehealth.abdm.databinding.FragmentMobileOtpVerificationBinding
import org.intelehealth.abdm.features.base.BaseFragment
import org.intelehealth.abdm.features.intent.RegistrationVerificationIntent
import org.intelehealth.abdm.features.viewmodel.registration.AadhaarCardVerificationViewModel
import org.intelehealth.abdm.features.viewstate.EnrollMobileOtpViewState
import org.intelehealth.abdm.features.viewstate.SendAadhaarOtpViewState

class MobileOtpVerificationFragment :
    BaseFragment<FragmentMobileOtpVerificationBinding, AadhaarCardVerificationViewModel>() {

    private lateinit var progressBarUtils: ProgressBarUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialization()

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnVerifyOtp.btnActive.text = getString(R.string.verify_otp)
        setClickListener()
        handleSendMobileOtpState()
        handleVerifyOtpOtpState()
    }

    override fun setClickListener() {
        binding.btnVerifyOtp.btnActive.setOnClickListener {
            viewModel.sendIntent(RegistrationVerificationIntent.OnClickVerifyAadhaarOtp(binding.otpBox.text.toString()))
        }
    }


    override fun initViewModel() = ViewModelProvider(
        requireActivity()
    )[AadhaarCardVerificationViewModel::class.java]

    override fun initBinding() =
        FragmentMobileOtpVerificationBinding.inflate(layoutInflater)

    private fun initialization() {
        progressBarUtils = ProgressBarUtils(requireContext())
    }

    private fun handleSendMobileOtpState() {
        viewModel.sendAadhaarOtpState.observe(this) {
            when (it) {
                is SendAadhaarOtpViewState.Idle -> {

                }

                is SendAadhaarOtpViewState.Loading -> progressBarUtils.showLinearProgressbar()

                is SendAadhaarOtpViewState.Success -> progressBarUtils.dismissProgressBar()

                is SendAadhaarOtpViewState.Error -> {
                    progressBarUtils.dismissProgressBar()
                    ToastUtil.showShortToast(requireContext(), it.message)
                }
            }
        }
    }

    private fun handleVerifyOtpOtpState() {
        viewModel.enrollMobileOtpState.observe(this) {
            when (it) {
                is EnrollMobileOtpViewState.Idle -> {

                }

                is EnrollMobileOtpViewState.Loading -> {
                    progressBarUtils.showLinearProgressbar()
                }

                is EnrollMobileOtpViewState.OpenSelectAbhaScreen -> {
                    progressBarUtils.dismissProgressBar()
                }
                is EnrollMobileOtpViewState.Error -> {
                    progressBarUtils.dismissProgressBar()
                    ToastUtil.showShortToast(requireContext(), it.message)
                }

            }
        }
    }
}

