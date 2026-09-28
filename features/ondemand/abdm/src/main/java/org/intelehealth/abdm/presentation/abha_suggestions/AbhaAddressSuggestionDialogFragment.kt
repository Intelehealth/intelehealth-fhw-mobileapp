package org.intelehealth.abdm.presentation.abha_suggestions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.intelehealth.abdm.R
import org.intelehealth.abdm.databinding.DialogAbhaAddressSuggestionBinding

/** Help sheet explaining the ABHA-address rules. Static content; ports the legacy dialog verbatim. */
internal class AbhaAddressSuggestionDialogFragment : BottomSheetDialogFragment() {

    override fun getTheme(): Int = R.style.Theme_Abdm_BottomSheet

    private var binding: DialogAbhaAddressSuggestionBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = DialogAbhaAddressSuggestionBinding.inflate(inflater, container, false)
            .also { this.binding = it }
        binding.ivClose.setOnClickListener { dismiss() }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    companion object {
        const val TAG = "AbhaAddressSuggestionDialog"
    }
}
