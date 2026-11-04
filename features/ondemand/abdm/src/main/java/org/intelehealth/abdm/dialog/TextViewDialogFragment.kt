package org.intelehealth.abdm.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.DialogFragment
import org.intelehealth.abdm.databinding.DialogTextViewBinding
import org.intelehealth.abdm.utils.DialogUtils

class TextViewDialogFragment(
    private val subtitleText: String,
    private val errorMessage: String,
    private val listener: DialogUtils.TextSelectedListener
) : DialogFragment() {

    private lateinit var binding: DialogTextViewBinding

    companion object {
        const val TAG = "TextViewDialogFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogTextViewBinding.inflate(layoutInflater)
        binding.dialogSubtitle.text = subtitleText
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvEntry.doOnTextChanged { text, _, _, _ ->
            binding.tvEntry.error = null
        }

        binding.positiveBtn.setOnClickListener {
            val enteredText: String = binding.tvEntry.text?.toString()?.trim() ?: ""

            if (!isTextValid(enteredText)) {
                binding.tvEntry.error = errorMessage
            } else {
                listener.onDialogActionDone(
                    DialogUtils.TextSelectedListener.POSITIVE_CLICK,
                    enteredText
                )
                dismiss()
            }
        }

        binding.negativeBtn.setOnClickListener {
            listener.onDialogActionDone(DialogUtils.TextSelectedListener.NEGATIVE_CLICK)
            dismiss()
        }
    }

    private fun isTextValid(enteredText: String): Boolean = enteredText.isNotBlank()

}