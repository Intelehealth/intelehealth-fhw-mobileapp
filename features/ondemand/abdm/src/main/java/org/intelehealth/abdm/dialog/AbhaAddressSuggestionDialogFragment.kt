package org.intelehealth.abdm.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.intelehealth.abdm.databinding.DialogAbhaAddressSuggestionBinding

class AbhaAddressSuggestionDialogFragment : BottomSheetDialogFragment() {
    private lateinit var binding: DialogAbhaAddressSuggestionBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogAbhaAddressSuggestionBinding.inflate(layoutInflater)
        setListeners()
        return binding.root
    }

    private fun setListeners() {
        binding.ivClose.setOnClickListener {
            dismiss()
        }
    }
}