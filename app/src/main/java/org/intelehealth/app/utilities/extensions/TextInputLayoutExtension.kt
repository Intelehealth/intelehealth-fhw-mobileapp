package org.intelehealth.app.utilities.extensions

import android.text.InputFilter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import androidx.annotation.StringRes
import androidx.core.widget.doOnTextChanged
import com.github.ajalt.timberkt.Timber
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
    input.doOnTextChanged { text, _, _, count ->
        Timber.d { "phone validation $count == $digit" }
        if (text?.length == digit) hideError()
    }
}

fun TextInputLayout.validate(input: TextInputEditText, @StringRes resId: Int): Boolean {
//    input.text?.let {
//        Timber.d { "Input data => ${it.isNotEmpty()} => $it" }
//        if (it.toString().isNotEmpty()) return true
//        else {
//            showError(resId)
//            return false
//        }
//    } ?: return false
    return if (input.text.isNullOrEmpty()) {
        showError(resId)
        false
    } else true
}

fun TextInputLayout.validateDropDowb(input: AutoCompleteTextView, @StringRes resId: Int): Boolean {
    return if (input.text.isNullOrEmpty()) {
        showError(resId)
        false
    } else true
}

fun TextInputLayout.validateDigit(
    input: TextInputEditText,
    @StringRes resId: Int,
    minDigit: Int
): Boolean {
    return if (input.text.isNullOrEmpty() || input.text?.length!! < minDigit) {
        showError(resId)
        false
    } else true
}

fun EditText.addFilter(filter: InputFilter) {
    this.filters = this.filters + filter
}