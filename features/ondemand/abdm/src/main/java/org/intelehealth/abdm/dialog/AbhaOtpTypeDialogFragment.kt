package org.intelehealth.abdm.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.intelehealth.abdm.constants.AbdmConstant.ABHA_OTP_AADHAAR
import org.intelehealth.abdm.constants.AbdmConstant.ABHA_OTP_MOBILE
import org.intelehealth.abdm.databinding.FragmentAbhaOtpTypeDialogBinding

import org.intelehealth.abdm.R;

class AbhaOtpTypeDialogFragment : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentAbhaOtpTypeDialogBinding
    private var onAuthTypeSelection: OnAuthTypeSelection? = null
    private var authType = ABHA_OTP_AADHAAR
    private var authMethods = ABHA_OTP_AADHAAR


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentAbhaOtpTypeDialogBinding.inflate(layoutInflater)
        setUi()
        setListeners()
        return binding.root
    }

    fun setUi() {
        if (authMethods.equals(ABHA_OTP_AADHAAR)) {
            binding.btnAbha.visibility = View.GONE
        } else if (authMethods.equals(ABHA_OTP_MOBILE)) {
            binding.btnAadhaar.visibility = View.GONE
        } else {
            binding.btnAadhaar.visibility = View.VISIBLE
            binding.btnAbha.visibility = View.VISIBLE
        }
    }

    private fun setListeners() {
        binding.btnContinue.setOnClickListener {
            onAuthTypeSelection?.continueOtp(authType)
            dismiss()
        }
        binding.btnAadhaar.setOnClickListener {
            authType = ABHA_OTP_AADHAAR

            binding.btnAadhaar.setBackgroundResource(
                R.drawable.bg_btn_abha_option
            )
            binding.btnAbha.setBackgroundResource(
                R.drawable.bg_btn_abha_option_disabled
            )

        }
        binding.btnAbha.setOnClickListener {
            authType = ABHA_OTP_MOBILE

            binding.btnAadhaar.setBackgroundResource(
                R.drawable.bg_btn_abha_option_disabled
            )
            binding.btnAbha.setBackgroundResource(
                R.drawable.bg_btn_abha_option
            )
        }
    }

    fun openAuthSelectionDialogDialog(
        authMethods: String,
        onAuthTypeSelection: OnAuthTypeSelection,
    ) {
        this.onAuthTypeSelection = onAuthTypeSelection
        this.authMethods = authMethods
    }

    interface OnAuthTypeSelection {
        fun continueOtp(authType: String)
    }


}