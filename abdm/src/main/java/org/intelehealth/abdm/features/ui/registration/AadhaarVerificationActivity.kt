package org.intelehealth.abdm.features.ui.registration

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import org.intelehealth.abdm.R
import org.intelehealth.abdm.databinding.ActivityAadhaarVerificationBinding
import org.intelehealth.abdm.features.base.BaseActivity
import org.intelehealth.abdm.features.viewmodel.registration.AadhaarCardVerificationViewModel

class AadhaarVerificationActivity :
    BaseActivity<ActivityAadhaarVerificationBinding, AadhaarCardVerificationViewModel>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialization()
        setClickListener()
    }

    private fun initialization() {
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
}