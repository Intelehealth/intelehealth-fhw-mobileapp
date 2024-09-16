package org.intelehealth.abdm.features.ui.registration

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import dagger.hilt.android.AndroidEntryPoint
import org.intelehealth.abdm.R
import org.intelehealth.abdm.common.utils.ProgressBarUtils
import org.intelehealth.abdm.common.utils.ToastUtil
import org.intelehealth.abdm.databinding.ActivityAadhaarVerificationBinding
import org.intelehealth.abdm.features.base.BaseActivity
import org.intelehealth.abdm.features.viewmodel.registration.AadhaarCardVerificationViewModel
import org.intelehealth.abdm.features.viewstate.SendAadhaarOtpViewState

@AndroidEntryPoint
class AadhaarVerificationActivity :
    BaseActivity<ActivityAadhaarVerificationBinding, AadhaarCardVerificationViewModel>() {
    private lateinit var progressBarDialog: ProgressBarUtils
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialization()
        handleSendOtpState()
        setClickListener()
    }

    private fun initialization() {
        progressBarDialog = ProgressBarUtils(this)
        binding.toolbar.tvToolbarText.text = getString(R.string.otp_verification)
        setAadhaarVerificationFragment()
    }

    override fun setClickListener() {
        binding.toolbar.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setAadhaarVerificationFragment() {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.flVerificationLayout, AadhaarCardVerificationFragment())
            .commit()
    }


    private fun setOtpVerificationFragment() {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.flVerificationLayout, AadhaarOtpVerificationFragment())
            .addToBackStack(null).commit()
    }

    override fun initViewModel() =
        ViewModelProvider(this)[AadhaarCardVerificationViewModel::class.java]

    override fun initBinding() = ActivityAadhaarVerificationBinding.inflate(layoutInflater)

    private fun handleSendOtpState() {
        viewModel.sendAadhaarOtpState.observe(this) {
            when (it) {
                is SendAadhaarOtpViewState.Idle -> {}
                is SendAadhaarOtpViewState.Loading -> {
                    progressBarDialog.showCircularProgressbar()
                }

                is SendAadhaarOtpViewState.Success -> {
                    progressBarDialog.dismissProgressBar()
                    setOtpVerificationFragment()
                }

                is SendAadhaarOtpViewState.Error -> {
                    ToastUtil.showLongToast(this,it.message)
                    progressBarDialog.dismissProgressBar()
                }
            }
        }

    }

}