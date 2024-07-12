package org.intelehealth.app.utilities.extensions

import android.text.InputFilter
import android.widget.EditText
import androidx.annotation.StringRes
import androidx.core.widget.doOnTextChanged
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

/**
 * Created by Vaghela Mithun R. on 12-07-2024 - 11:05.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/

fun TextInputLayout.showError(@StringRes resId: Int) {
    error = context.getString(resId)
}

fun TextInputLayout.hideError() {
    isErrorEnabled = false
}

fun TextInputLayout.hideErrorOnTextChang(input: TextInputEditText) {
    input.doOnTextChanged { _, _, _, count ->
        if (count > 0) hideError()
    }
}

fun TextInputLayout.hideDigitErrorOnTextChang(input: TextInputEditText, digit: Int) {
    input.doOnTextChanged { _, _, _, count ->
        if (count == digit) hideError()
    }
}

fun TextInputLayout.validate(input: TextInputEditText, @StringRes resId: Int): Boolean {
    input.text?.let {
        if (it.isNotEmpty()) return true
        else {
            showError(resId)
            return false
        }
    } ?: return false
}

fun TextInputLayout.validateDigit(
    input: TextInputEditText,
    @StringRes resId: Int,
    minDigit: Int
): Boolean {
    input.text?.let {
        if (it.isNotEmpty() && it.length == minDigit) return true
        else {
            showError(resId)
            return false
        }
    } ?: return false
}

fun EditText.addFilter(filter: InputFilter) {
    this.filters = this.filters + filter
}