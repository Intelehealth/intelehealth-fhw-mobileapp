package org.intelehealth.abdm.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.DialogFragment
import org.intelehealth.abdm.R
import org.intelehealth.abdm.databinding.DialogAbhaChoiceBinding
import org.intelehealth.abdm.listener.AbhaChoiceListener
import org.intelehealth.abdm.utils.DialogUtils

class AbhaChoiceDialogFragment : DialogFragment() {

    var listener: AbhaChoiceListener? = null
    private var binding: DialogAbhaChoiceBinding? = null

    companion object {
        const val TAG = "AbhaChoiceDialogFragment"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogAbhaChoiceBinding.inflate(layoutInflater)

        binding?.yesAbhaBtn?.setOnClickListener {
            dismiss()
            listener?.onHasAbha();
        }

        binding?.createAbhaBtn?.setOnClickListener {
            dismiss()
            listener?.onCreateAbha()
        }

        binding?.continueWithoutAbhaBtn?.setOnClickListener {
            dismiss()
            listener?.onContinueWithoutAbha()
        }

        val builder = AlertDialog.Builder(requireContext()).setView(binding?.root)
        return builder.create()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
            val width = resources.getDimensionPixelSize(R.dimen.abha_choice_dialog_width)
            setLayout(width, android.view.ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}