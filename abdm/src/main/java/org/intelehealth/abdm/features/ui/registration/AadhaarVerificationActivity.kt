package org.intelehealth.abdm.features.ui.registration

import android.content.Intent
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
import org.intelehealth.abdm.features.viewstate.VerifyAadhaarOtpViewState

@AndroidEntryPoint
class AadhaarVerificationActivity :
    BaseActivity<ActivityAadhaarVerificationBinding, AadhaarCardVerificationViewModel>() {
    private lateinit var progressBarDialog: ProgressBarUtils
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialization()
        handleSendOtpState()
        handleVerifyAadhaarOtp()
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


    private fun setAadhaarOtpVerificationFragment() {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.flVerificationLayout, AadhaarOtpVerificationFragment())
            .addToBackStack(null).commit()
    }
    private fun setNewMobileOtpVerificationFragment() {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.flVerificationLayout, MobileOtpVerificationFragment())
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
                    setAadhaarOtpVerificationFragment()
                }

                is SendAadhaarOtpViewState.Error -> {
                    ToastUtil.showLongToast(this,it.message)
                    progressBarDialog.dismissProgressBar()
                }

            }
        }

    }

    private fun handleVerifyAadhaarOtp()
    {
        viewModel.verifyAadhaarOtpState.observe(this) {
            when (it) {
                is VerifyAadhaarOtpViewState.Idle -> {}
                is VerifyAadhaarOtpViewState.Error ->  {
                    progressBarDialog.dismissProgressBar()
                    ToastUtil.showShortToast(this,it.message)
                }

                is VerifyAadhaarOtpViewState.Loading -> progressBarDialog.showCircularProgressbar()
                is VerifyAadhaarOtpViewState.OpenMobileVerificationScreen -> {
                    progressBarDialog.dismissProgressBar()
                    setNewMobileOtpVerificationFragment()
                }
                is VerifyAadhaarOtpViewState.OpenSelectAbhaScreen -> {
                    startActivity(Intent(this,SelectAbhaAddressActivity::class.java))
                }
            }
        }

    }

}