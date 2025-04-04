package org.intelehealth.abdm.features.ui.registration.dialog

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import org.intelehealth.abdm.R
import org.intelehealth.abdm.common.utils.ToastUtil
import org.intelehealth.abdm.databinding.DialogAbdmPatientRegistrationConfirmationBinding
import org.intelehealth.abdm.databinding.DialogAbdmRegistrationConfirmationBinding
import org.intelehealth.abdm.features.ui.registration.AadhaarVerificationActivity
import org.intelehealth.abdm.features.ui.registration.AbhaRegistrationConsentActivity


class RegistrationConfirmationDialog : DialogFragment() {
    private lateinit var mBinding: DialogAbdmPatientRegistrationConfirmationBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding =
            DialogAbdmPatientRegistrationConfirmationBinding.inflate(inflater, container, false)
        initialization()
        setClickListener()
        return mBinding.root
    }

    private fun initialization() {
        mBinding.btnAccept.btnActive.text = getString(R.string.yes)
        mBinding.btnCAbha.btnDecline.text = getString(R.string.create_abha)
        mBinding.btnCWAbha.btnDecline.text = getString(R.string.continue_without_abha)
    }

    private fun setClickListener() {
        mBinding.btnAccept.btnActive.setOnClickListener {
            ToastUtil.showShortToast(requireContext(), "Work in progress")
        }
        mBinding.btnCAbha.btnDecline.setOnClickListener {
             startActivity(Intent(requireContext(), AadhaarVerificationActivity::class.java))
        }
        mBinding.btnCWAbha.btnDecline.setOnClickListener {
            ToastUtil.showShortToast(requireContext(), "Work in progress")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        return super.onCreateDialog(savedInstanceState)

    }

}

