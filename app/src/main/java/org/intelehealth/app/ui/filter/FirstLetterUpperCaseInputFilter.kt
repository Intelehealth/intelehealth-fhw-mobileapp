package org.intelehealth.app.ui.filter

import android.text.InputFilter
import android.text.Spanned

class FirstLetterUpperCaseInputFilter : InputFilter {
    override fun filter(
        source: CharSequence,
        start: Int,
        end: Int,
        dest: Spanned,
        dstart: Int,
        dend: Int
    ): CharSequence {
        val input = source.subSequence(start, end).toString()
        if (dstart == 0 && source.isNotEmpty()) {
            // Capitalize the first character if it's at the beginning of the text
            val firstChar = source[0].uppercase()
            val restOfInput = if (input.length > 1) input.substring(1) else ""
            return firstChar + restOfInput
        }

        // If the filtered text differs from the original, return it
        return source
    }
}