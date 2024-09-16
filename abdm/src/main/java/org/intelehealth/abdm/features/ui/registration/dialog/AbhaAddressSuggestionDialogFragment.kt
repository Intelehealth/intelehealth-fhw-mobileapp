package org.intelehealth.abdm.features.ui.registration.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.intelehealth.abdm.R
import org.intelehealth.abdm.databinding.DialogAbhaAddressSuggestionBinding


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