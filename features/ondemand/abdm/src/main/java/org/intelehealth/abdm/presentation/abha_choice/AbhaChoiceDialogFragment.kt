package org.intelehealth.abdm.presentation.abha_choice

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.core.graphics.drawable.toDrawable
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import org.intelehealth.abdm.R
import org.intelehealth.abdm.databinding.DialogAbhaChoiceBinding
import org.intelehealth.abdm.presentation.common.abdmDialogInflater

/**
 * Entry point for the ABDM module.
 *
 * The host shows this dialog and listens for the user's [Choice]. The dialog is fully
 * decoupled — the host implements no interface and holds no reference to the instance,
 * and the result survives configuration changes (it uses the Fragment Result API).
 *
 * Typical usage from a host Activity/Fragment:
 * ```
 * AbhaChoiceDialogFragment.setResultListener(supportFragmentManager, this) { choice ->
 *     when (choice) {
 *         AbhaChoiceDialogFragment.Choice.VERIFY_ABHA -> // launch AbhaVerifyActivity
 *         AbhaChoiceDialogFragment.Choice.CREATE_ABHA -> // launch AbhaCreateActivity
 *         AbhaChoiceDialogFragment.Choice.CONTINUE_WITHOUT_ABHA -> // proceed without ABHA
 *     }
 * }
 * AbhaChoiceDialogFragment.show(supportFragmentManager)
 * ```
 */
class AbhaChoiceDialogFragment : DialogFragment() {

    private var binding: DialogAbhaChoiceBinding? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = DialogAbhaChoiceBinding.inflate(abdmDialogInflater()).also { this.binding = it }

        binding.btnYesAbha.setOnClickListener { deliver(Choice.VERIFY_ABHA) }
        binding.btnCreateAbha.setOnClickListener { deliver(Choice.CREATE_ABHA) }
        binding.btnContinueWithoutAbha.setOnClickListener { deliver(Choice.CONTINUE_WITHOUT_ABHA) }

        return AlertDialog.Builder(requireContext(), R.style.Theme_Abdm_Dialog)
            .setView(binding.root)
            .create()
    }

    private fun deliver(choice: Choice) {
        parentFragmentManager.setFragmentResult(REQUEST_KEY, bundleOf(RESULT_CHOICE to choice.name))
        dismiss()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
            setLayout(
                resources.getDimensionPixelSize(R.dimen.abdm_choice_dialog_width),
                WRAP_CONTENT
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    /** The options the user can pick from this dialog. */
    enum class Choice { VERIFY_ABHA, CREATE_ABHA, CONTINUE_WITHOUT_ABHA }

    companion object {
        const val TAG = "AbhaChoiceDialogFragment"
        const val REQUEST_KEY = "abdm_abha_choice_request"
        private const val RESULT_CHOICE = "abdm_abha_choice_result"

        /** Shows the dialog. Pair with [setResultListener] to receive the user's choice. */
        @JvmStatic
        fun show(fragmentManager: FragmentManager) {
            AbhaChoiceDialogFragment().show(fragmentManager, TAG)
        }

        /**
         * Registers a typed listener for the dialog's result. Safe to call in onCreate;
         * the listener is bound to [lifecycleOwner] and survives configuration changes.
         */
        @JvmStatic
        fun setResultListener(
            fragmentManager: FragmentManager,
            lifecycleOwner: LifecycleOwner,
            onChoice: (Choice) -> Unit,
        ) {
            fragmentManager.setFragmentResultListener(REQUEST_KEY, lifecycleOwner) { _, bundle ->
                val name = bundle.getString(RESULT_CHOICE) ?: return@setFragmentResultListener
                onChoice(Choice.valueOf(name))
            }
        }
    }
}
