package org.intelehealth.app.abdm.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.intelehealth.app.R
import org.intelehealth.app.abdm.utils.ABDMConstant.ABHA_OTP_AADHAAR
import org.intelehealth.app.abdm.utils.ABDMConstant.ABHA_OTP_MOBILE
import org.intelehealth.app.databinding.DialogAbhaAddressSuggestionBinding
import org.intelehealth.app.databinding.FragmentAbhaOtpTypeDialogBinding


class AbhaAddressSuggestionDialogFragment : BottomSheetDialogFragment() {
    private lateinit var binding: DialogAbhaAddressSuggestionBinding



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.dialog_abha_address_suggestion,
            container,
            false
        )

        setListeners()
        return binding.root
    }

    private fun setListeners() {
        binding.ivClose.setOnClickListener {
            dismiss()
        }

    }





}