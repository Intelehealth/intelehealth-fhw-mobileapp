package org.intelehealth.app.ui.filter

import android.text.InputFilter
import android.text.Spanned
import org.intelehealth.app.utilities.StringUtils.inputFilter_Others

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

/*
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
            val name = firstChar + restOfInput
            return inputFilter_Others.filter(
                name,
                start,
                end,
                dest,
                dstart,
                dend
            )
        }

        // If the filtered text differs from the original, return it
        return inputFilter_Others.filter(
            source,
            start,
            end,
            dest,
            dstart,
            dend
        )
    }

}*/
