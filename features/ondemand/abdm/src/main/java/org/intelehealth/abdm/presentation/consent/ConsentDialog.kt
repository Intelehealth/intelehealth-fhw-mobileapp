package org.intelehealth.abdm.presentation.consent

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import org.intelehealth.abdm.R
import org.intelehealth.abdm.databinding.DialogConsentBinding

/**
 * The official ABHA consent dialog. Content and terminology are ported verbatim from the
 * legacy module; only the resource plumbing is module-local.
 */
class ConsentDialog(
    private val patientName: String,
    private val hwFullName: String? = null,
) : DialogFragment() {

    /** The module's Material components need a MaterialComponents theme; the host may not have one. */
    override fun getTheme(): Int = R.style.Theme_Abdm_Dialog

    private var isAccepted: Boolean = false
    private var clickable: Clickable? = null
    private var checkboxAdapter: CheckboxAdapter? = null
    private lateinit var binding: DialogConsentBinding

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DialogConsentBinding.inflate(layoutInflater)

        val modelList = mutableListOf(
            CheckBoxRecyclerModel(getString(R.string.abha_consent_line1) + NEW_LINE, false, true),
            CheckBoxRecyclerModel(getString(R.string.abha_consent_line2) + NEW_LINE, false, false),
            CheckBoxRecyclerModel(getString(R.string.abha_consent_line3) + NEW_LINE, false, true),
            CheckBoxRecyclerModel(getString(R.string.abha_consent_line4) + NEW_LINE, false, true),
            CheckBoxRecyclerModel(getString(R.string.abha_consent_line5) + NEW_LINE, false, true),
            CheckBoxRecyclerModel(
                getString(R.string.abha_consent_line6, fetchHwFullName()) + NEW_LINE,
                false,
                true,
            ),
            CheckBoxRecyclerModel(
                getString(R.string.abha_consent_line7, patientName) + NEW_LINE,
                false,
                true,
            ),
        )

        checkboxAdapter = CheckboxAdapter(requireContext(), modelList) {
            val allChecked = checkboxAdapter?.areAllItemsChecked() == true
            if (allChecked) {
                binding.btnAcceptPrivacy.isEnabled = true
                binding.btnAcceptPrivacy.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.abdm_bg_button_primary)
            } else {
                binding.btnAcceptPrivacy.isEnabled = false
                binding.btnAcceptPrivacy.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.abdm_bg_button_disabled)
            }
        }
        binding.rvAbhaConsent.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAbhaConsent.adapter = checkboxAdapter

        binding.btnAcceptPrivacy.setOnClickListener {
            isAccepted = true
            dismiss()
        }
        binding.btnDecline.setOnClickListener {
            isAccepted = false
            dismiss()
        }
        return binding.root
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        clickable?.isChecked(isAccepted)
    }

    fun setListeners(clickable: Clickable) {
        this.clickable = clickable
    }

    private fun fetchHwFullName(): String =
        hwFullName?.takeIf { it.isNotBlank() } ?: "(Health Worker)"

    companion object {
        const val ABHA_CONSENT: String = "ABHA_CONSENT"
        const val NEW_LINE: String = "<br>"
    }

    interface Clickable {
        fun isChecked(isCheck: Boolean)
    }
}
