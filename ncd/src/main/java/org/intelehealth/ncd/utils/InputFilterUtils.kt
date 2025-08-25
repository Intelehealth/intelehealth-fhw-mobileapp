package org.intelehealth.ncd.utils

import android.text.InputFilter
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils

val inputFilterSearchBar = InputFilter { source, start, end, dest, dstart, dend ->
    var keepOriginal = true
    val filtered = StringBuilder(end - start)

    for (i in start until end) {
        val c = source[i]
        if (isCharAllowed(c)) {
            filtered.append(c)
        } else {
            keepOriginal = false
        }
    }

    if (keepOriginal) {
        null
    } else {
        if (source is Spanned) {
            val spannableString = SpannableString(filtered)
            TextUtils.copySpansFrom(source, start, filtered.length, null, spannableString, 0)
            spannableString
        } else {
            filtered.toString()
        }
    }
}

private fun isCharAllowed(c: Char): Boolean {
    return c.isLetterOrDigit() ||
            Character.getType(c) == Character.NON_SPACING_MARK.toInt() ||
            Character.getType(c) == Character.COMBINING_SPACING_MARK.toInt() ||
            c == '-' || c == ' '
}
