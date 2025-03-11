package org.intelehealth.app.ui.filter

import android.text.InputFilter
import android.text.Spanned

// Filter to allow Characters, Digits, Special Characters, and Emojis
class LettersNumbersSelectedSymbolsInputFilter : InputFilter {

    override fun filter(
        source: CharSequence,
        start: Int,
        end: Int,
        dest: Spanned,
        dstart: Int,
        dend: Int
    ): CharSequence {
        val filteredInput = source.subSequence(start, end).filter {
            Character.isLetterOrDigit(it) || it in listOf('.', ',', '-', ' ')
        }
        return filteredInput
    }
}