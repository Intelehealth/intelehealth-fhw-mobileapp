package org.intelehealth.abdm.presentation.abha_create

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.core.graphics.drawable.toDrawable
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import org.intelehealth.abdm.R
import org.intelehealth.abdm.databinding.DialogPatientNameBinding
import org.intelehealth.abdm.presentation.common.abdmDialogInflater

/**
 * Collects the patient's name at the start of the create flow (used for the consent text and
 * downstream registration). Reports the entered name via the Fragment Result API; dismissing
 * without entering one is treated as cancelling the flow.
 */
internal class PatientNameDialogFragment : DialogFragment() {

    private var binding: DialogPatientNameBinding? = null
    private var submitted = false

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = DialogPatientNameBinding.inflate(abdmDialogInflater()).also { this.binding = it }

        binding.etPatientName.doAfterTextChanged { binding.tvNameError.isVisible = false }

        binding.btnContinue.setOnClickListener {
            val name = binding.etPatientName.text?.toString()?.trim().orEmpty()
            if (name.isEmpty()) {
                binding.tvNameError.isVisible = true
                return@setOnClickListener
            }
            submitted = true
            parentFragmentManager.setFragmentResult(REQUEST_KEY, bundleOf(RESULT_NAME to name))
            dismiss()
        }

        return AlertDialog.Builder(requireContext(), R.style.Theme_Abdm_Dialog)
            .setView(binding.root)
            .create()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
            setLayout(resources.getDimensionPixelSize(R.dimen.abdm_choice_dialog_width), WRAP_CONTENT)
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        if (!submitted) {
            parentFragmentManager.setFragmentResult(REQUEST_KEY, bundleOf(RESULT_NAME to null))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    companion object {
        const val TAG = "PatientNameDialog"
        private const val REQUEST_KEY = "abdm_patient_name_request"
        private const val RESULT_NAME = "name"

        fun show(fragmentManager: FragmentManager) {
            PatientNameDialogFragment().show(fragmentManager, TAG)
        }

        /** [onName] fires with the entered name; [onCancelled] fires if the dialog is dismissed without one. */
        fun setResultListener(
            fragmentManager: FragmentManager,
            lifecycleOwner: LifecycleOwner,
            onName: (String) -> Unit,
            onCancelled: () -> Unit,
        ) {
            fragmentManager.setFragmentResultListener(REQUEST_KEY, lifecycleOwner) { _, bundle ->
                val name = bundle.getString(RESULT_NAME)
                if (name.isNullOrBlank()) onCancelled() else onName(name)
            }
        }
    }
}
