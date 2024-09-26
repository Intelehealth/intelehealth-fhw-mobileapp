package org.intelehealth.abdm.features.ui.registration

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import org.intelehealth.abdm.R
import org.intelehealth.abdm.common.constant.Constants.AADHAAR_LENGTH
import org.intelehealth.abdm.common.constant.Constants.MOBILE_NUMBER_LENGTH
import org.intelehealth.abdm.common.extension.gone
import org.intelehealth.abdm.common.extension.visible
import org.intelehealth.abdm.common.utils.ValidationUtils
import org.intelehealth.abdm.databinding.FragmentAadhaarCardVerificationBinding
import org.intelehealth.abdm.features.base.BaseFragment
import org.intelehealth.abdm.features.intent.RegistrationVerificationIntent
import org.intelehealth.abdm.features.viewmodel.registration.AadhaarCardVerificationViewModel

class AadhaarCardVerificationFragment :
    BaseFragment<FragmentAadhaarCardVerificationBinding, AadhaarCardVerificationViewModel>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnSendOtp.btnActive.text = getString(R.string.send_otp)
        binding.btnSendOtp.btnActive.isEnabled = false
        setClickListener()
        setTextListeners()
    }

    override fun setClickListener() {
        binding.btnSendOtp.btnActive.setOnClickListener {
            if (isValidAadhaar(binding.etAadhaarNo.text.toString()) && isValidPhoneNumber(binding.etPhoneNo.text.toString())) {
                viewModel.sendIntent(
                    RegistrationVerificationIntent.OnClickSendOtp(
                        binding.etAadhaarNo.text.toString(),
                        binding.etPhoneNo.text.toString()
                    )
                )
            }
        }
    }

    override fun initViewModel(): AadhaarCardVerificationViewModel = ViewModelProvider(
        requireActivity()
    )[AadhaarCardVerificationViewModel::class.java]

    override fun initBinding() = FragmentAadhaarCardVerificationBinding.inflate(layoutInflater)

    private fun setTextListeners() {
        binding.etAadhaarNo.doAfterTextChanged {
            binding.btnSendOtp.btnActive.isEnabled =
                !binding.etAadhaarNo.text.isNullOrBlank() && !binding.etPhoneNo.text.isNullOrBlank()
            isValidAadhaar(it.toString())
        }
        binding.etPhoneNo.doAfterTextChanged {
            binding.btnSendOtp.btnActive.isEnabled =
                !binding.etAadhaarNo.text.isNullOrBlank() && !binding.etPhoneNo.text.isNullOrBlank()
            isValidPhoneNumber(it.toString())
        }
    }

    private fun isValidAadhaar(aadhaar: String): Boolean {
        if (TextUtils.isEmpty(aadhaar)) {
            binding.tvAadhaarError.visible()
            binding.tvAadhaarError.text = getString(R.string.error_field_required)
            return false
        } else if (aadhaar.length < AADHAAR_LENGTH) {
            binding.tvAadhaarError.visible()
            binding.tvAadhaarError.text = getString(R.string.enter_12_digits)
            return false
        } else if (!ValidationUtils.isValidAadhaar(aadhaar)) {
            binding.tvAadhaarError.visible()
            binding.tvAadhaarError.text = getString(R.string.aadhaar_number_is_not_valid)
            return false
        } else {
            binding.tvAadhaarError.gone()
            return true
        }
    }

    private fun isValidPhoneNumber(mobileNo: String): Boolean {
        if (TextUtils.isEmpty(mobileNo)) {
            binding.tvPhoneError.visible()
            binding.tvPhoneError.text = getString(R.string.error_field_required)
            return false
        } else if (mobileNo.length < MOBILE_NUMBER_LENGTH) {
            binding.tvPhoneError.visible()
            binding.tvAadhaarError.text = getString(R.string.enter_12_digits)
            return false
        } else {
            binding.tvPhoneError.gone()
            return true
        }
    }
}