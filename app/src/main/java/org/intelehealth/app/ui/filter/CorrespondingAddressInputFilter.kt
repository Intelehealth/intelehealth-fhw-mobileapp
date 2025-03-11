package org.intelehealth.app.ui.filter

import android.text.InputFilter
import android.text.Spanned

// Filter to allow Characters, Digits, Special Characters, and Emojis
class CorrespondingAddressInputFilter : InputFilter {

    private val allowedChars = "[\\p{L}0-9.,\\-\\s]"

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