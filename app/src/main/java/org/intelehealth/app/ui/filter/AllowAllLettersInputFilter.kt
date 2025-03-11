package org.intelehealth.app.ui.filter

import android.text.InputFilter
import android.text.Spanned

class AllowAllCharactersInputFilter : InputFilter {

    override fun filter(
        source: CharSequence,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int
    ): CharSequence {
        val filteredInput = source.subSequence(start, end).filter { Character.isLetter(it) }
        return filteredInput
    }

}