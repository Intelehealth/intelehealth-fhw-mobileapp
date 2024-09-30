package org.intelehealth.abdm.features.ui.registration

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import org.intelehealth.abdm.R
import org.intelehealth.abdm.common.utils.ProgressBarUtils
import org.intelehealth.abdm.common.utils.ToastUtil
import org.intelehealth.abdm.databinding.FragmentAadhaarOtpVerificationBinding
import org.intelehealth.abdm.features.base.BaseFragment
import org.intelehealth.abdm.features.intent.RegistrationVerificationIntent
import org.intelehealth.abdm.features.viewmodel.registration.AadhaarCardVerificationViewModel
import org.intelehealth.abdm.features.viewstate.SendAadhaarOtpViewState

class AadhaarOtpVerificationFragment :
    BaseFragment<FragmentAadhaarOtpVerificationBinding, AadhaarCardVerificationViewModel>() {

    private var countDownTimer: CountDownTimer? = null
    private var resendCounter: Int = 2
    private lateinit var progressBarUtils: ProgressBarUtils
    private   var timer = 60000L


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnVerifyOtp.btnActive.text = getString(R.string.verify_otp)
        binding.btnVerifyOtp.btnActive.isEnabled = false
        progressBarUtils = ProgressBarUtils(requireContext())
        resendCounterAttemptsTextDisplay()
        resendOtp()
        setClickListener()
        handleSendAadhaarOtp()
    }

    override fun setClickListener() {
        binding.btnVerifyOtp.btnActive.setOnClickListener {
            viewModel.sendIntent(RegistrationVerificationIntent.OnClickVerifyAadhaarOtp(binding.otpBox.text.toString()))
        }
        binding.resendLayout.tvResendBtn.setOnClickListener {
            if (resendCounter != 0) {
                resendCounter--
                resendCounterAttemptsTextDisplay()
                resendOtp()
                viewModel.sendIntent(
                    RegistrationVerificationIntent.OnClickSendOtp(

                        aadhaarNo = viewModel.aadhaarNo,
                        mobileNo = viewModel.enteredMobileNumber
                    )
                )
                binding.otpBox.setText("")

            } else {
                resendCounterAttemptsTextDisplay()
            }
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

    private fun handleSendAadhaarOtp() {
        viewModel.reSendAadhaarOtpState.observe(viewLifecycleOwner) {
            when (it) {
                is SendAadhaarOtpViewState.Idle -> {}
                is SendAadhaarOtpViewState.Loading -> {
                    progressBarUtils.showLinearProgressbar()
                }

                is SendAadhaarOtpViewState.Success -> {
                    progressBarUtils.dismissProgressBar()
                    ToastUtil.showShortToast(
                        requireContext(),
                        getString(R.string.otp_sent_to_your_registered_mobile_number)
                    )
                }

                is SendAadhaarOtpViewState.Error -> {
                    ToastUtil.showShortToast(requireContext(), it.message)
                }
            }
        }
    }

    private fun resendCounterAttemptsTextDisplay() {
        if (resendCounter != 0) {
            binding.resendLayout.tvResendCounter.text = resources.getString(
                R.string.number_of_retries_left,
                resendCounter
            )
            timer = 60000
        } else {
            binding.resendLayout.tvResendCounter.text =
                getString(R.string.maximum_number_of_retries_exceeded_please_try_again_after_10_mins)
            timer = 600000
            resendCounter = 2
            binding.resendLayout.tvResendBtn.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.medium_gray
                )
            )
        }
    }

    private fun resendOtp() {

        val resendTime = resources.getString(R.string.resend_otp_in)

        if (countDownTimer != null) countDownTimer!!.cancel() // reset any existing countdown.

        countDownTimer = object : CountDownTimer(timer, 1000) { // 10 minutes = 600000 ms
            override fun onTick(millisUntilFinished: Long) {
                binding.resendLayout.tvResendBtn.isEnabled = false

                // Convert millisUntilFinished to minutes and seconds
                val minutes = (millisUntilFinished / 1000) / 60
                val seconds = (millisUntilFinished / 1000) % 60

                // Format the time as "MM:SS"
                val time = String.format("%02d:%02d", minutes, seconds)

                // Update the UI with the formatted time
                binding.resendLayout.tvResendBtn.text = "$resendTime $time"
            }

            override fun onFinish() {
                if (resendCounter != 0) {
                    binding.resendLayout.tvResendBtn.isEnabled = true
                    binding.resendLayout.tvResendBtn.setTextColor(
                        ContextCompat.getColor(requireContext(), R.color.colorPrimary)
                    )
                }

                binding.resendLayout.tvResendBtn.text = resources.getString(R.string.resend_otp)
            }
        }.start()
    }
}
