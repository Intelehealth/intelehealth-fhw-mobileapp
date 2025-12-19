package org.intelehealth.app.widget.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import org.intelehealth.app.R
import org.intelehealth.app.databinding.DialogCheckboxBinding
import org.intelehealth.app.utilities.DialogUtils

class ChecklistDialogFragment(
    private val abhaAddressList: List<String>,
    private val listener: DialogUtils.TextSelectedListener
) : DialogFragment() {

    private lateinit var binding: DialogCheckboxBinding

    companion object {
        const val TAG = "ChecklistDialogFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogCheckboxBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onStart() {
        super.onStart()

        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRadioGroup()

        binding.positiveBtn.setOnClickListener {
            val checkedId: Int = binding.rgAbhaAddress.checkedRadioButtonId
            if (checkedId == -1) {
                Toast.makeText(
                    context,
                    getString(R.string.please_select_an_abha_address_before_proceeding),
                    Toast.LENGTH_LONG
                ).show()

                return@setOnClickListener
            }

            val selectedAbhaAddress: String = getSelectedAbhaAddress(view)
            listener.onDialogActionDone(
                DialogUtils.TextSelectedListener.POSITIVE_CLICK,
                selectedAbhaAddress
            )
        }

        binding.negativeBtn.setOnClickListener {
            listener.onDialogActionDone(DialogUtils.TextSelectedListener.NEGATIVE_CLICK)
        }
    }

    fun displayError(errorMessage: String) {
        binding.tvError.text = errorMessage
    }

    fun shouldShowErrorMessage(shouldShow: Boolean) {
        binding.tvError.visibility = if (shouldShow) View.VISIBLE else View.GONE
    }

    private fun setupRadioGroup() {
        for (address in abhaAddressList) {
            val radioButton = RadioButton(context)
            radioButton.apply {
                id = View.generateViewId()
                text = address
            }
            binding.rgAbhaAddress.addView(radioButton)
        }

        binding.rgAbhaAddress.setOnCheckedChangeListener { _, checkedId ->
            if (binding.tvError.isVisible) {
                shouldShowErrorMessage(false)
            }
        }
    }

    private fun getSelectedAbhaAddress(view: View): String {
        val selectedRadioButtonId = binding.rgAbhaAddress.checkedRadioButtonId
        val selectedRadioButton: RadioButton = view.findViewById<RadioButton>(selectedRadioButtonId)
        return selectedRadioButton.text.toString()
    }
}