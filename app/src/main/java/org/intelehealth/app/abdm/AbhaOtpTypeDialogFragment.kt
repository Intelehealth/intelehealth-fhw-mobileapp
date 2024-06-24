package org.intelehealth.app.abdm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.intelehealth.app.R
import org.intelehealth.app.abdm.utils.ABDMConstant.ABHA_OTP_AADHAAR
import org.intelehealth.app.abdm.utils.ABDMConstant.ABHA_OTP_MOBILE
import org.intelehealth.app.databinding.FragmentAbhaOtpTypeDialogBinding


class AbhaOtpTypeDialogFragment : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentAbhaOtpTypeDialogBinding
    private var onAuthTypeSelection: OnAuthTypeSelection? = null
    private var authType = ABHA_OTP_AADHAAR




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_abha_otp_type_dialog,
            container,
            false
        )

        setListeners()
        return binding.root
    }

    private fun setListeners() {
        binding.btnContinue.setOnClickListener {
            onAuthTypeSelection?.continueOtp(authType)
            dismiss()
        }
        binding.btnAadhaar.setOnClickListener {
            authType = ABHA_OTP_AADHAAR

            binding.btnAadhaar.setBackgroundResource(
                R.drawable.button_bg_forgot_pass_ui2
            )
            binding.btnAbha.setBackgroundResource(
                R.drawable.button_bg_forgot_pass_disabled_ui2
            )

        }
        binding.btnAbha.setOnClickListener {
            authType = ABHA_OTP_MOBILE

            binding.btnAadhaar.setBackgroundResource(
                R.drawable.button_bg_forgot_pass_disabled_ui2
            )
            binding.btnAbha.setBackgroundResource(
                R.drawable.button_bg_forgot_pass_ui2
            )
        }
    }

    fun openAuthSelectionDialogDialog(
        onAuthTypeSelection: OnAuthTypeSelection,
    ) {
        this.onAuthTypeSelection = onAuthTypeSelection
    }

    interface OnAuthTypeSelection {
        fun continueOtp(authType: String)
    }


}