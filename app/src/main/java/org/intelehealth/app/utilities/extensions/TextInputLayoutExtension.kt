package org.intelehealth.app.utilities.extensions

import android.content.Context
import android.text.InputFilter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import androidx.annotation.ArrayRes
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import com.github.ajalt.timberkt.Timber
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.intelehealth.app.utilities.LanguageUtils

/**
 * Created by Vaghela Mithun R. on 12-07-2024 - 11:05.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/

fun TextInputLayout.showError(@StringRes resId: Int) {
    Timber.d { "showError" }
    error = context.getString(resId)
}

fun TextInputLayout.hideError() {
    Timber.d { "hideError" }
    isErrorEnabled = false
}

fun TextInputLayout.showError() {
    Timber.d { "showError" }
    isErrorEnabled = false
}

fun TextInputLayout.hideErrorOnTextChang(input: TextInputEditText) {
    input.doOnTextChanged { text, _, _, count ->
        Timber.d { "hideErrorOnTextChang" }
        if (text?.length!! > 0) hideError()
    }
}

fun TextInputLayout.hideDigitErrorOnTextChang(input: TextInputEditText, digit: Int) {
    input.doOnTextChanged { text, _, _, count ->
        Timber.d { "phone validation $count == $digit" }
        if (text?.length == digit) hideError() else showError()
    }
}


fun TextInputLayout.validate(input: TextInputEditText, @StringRes resId: Int): Boolean {
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

fun TextInputLayout.validateIllogicalPhoneNumber(
    input: TextInputEditText,
    @StringRes resId: Int
): Boolean {
    val illogicalNumber = listOf(
        "0000000000", "1111111111", "2222222222", "3333333333", "4444444444", "5555555555", "6666666666", "7777777777",
        "8888888888", "9999999999", "0123456789", "1234567890", "0000011111", "1111100000", "1111122222", "1122334455",
        "1212121212", "0101010101", "1234567891", "1234512345", "0000088888", "0909090909", "1123456789", "1234567899"
    )

    return if (illogicalNumber.contains(input.text.toString())) {
        showError(resId)
        false
    } else true
}

fun EditText.addFilter(filter: InputFilter) {
    this.filters = this.filters + filter
}

fun TextInputLayout.validateNumberOfUsualMembers(
    usualMembersInput: TextInputEditText,
    totalMembersInput: TextInputEditText,
    @StringRes resId: Int
): Boolean {
    val usualMembers: String = usualMembersInput.text.toString()
    val totalMembers: String = totalMembersInput.text.toString()
    return if (usualMembers.isNotEmpty() || totalMembers.isNotEmpty()) {
        //having problem to parse phone number to integer as phone number is too long
        //so, converting string to long integer
        val usual = usualMembers.toLongOrNull()
        val total = totalMembers.toLongOrNull()

        if (usual == null || total == null) {
            showError(resId)
            false
        } else{
            if (usual > total) {
                showError(resId)
                false
            } else true
        }
    } else false
}

fun TextInputLayout.validateIntegerDataLimits(
    valueEditText: TextInputEditText,
    startLimit: Int,
    endLimit: Int,
    @StringRes resId: Int
): Boolean {
    val input = valueEditText.text.toString()
    return if (input.isNotEmpty()) {
        val enteredValue: Int = input.toInt()
        if (enteredValue in startLimit..endLimit) {
            true
        } else {
            showError(resId)
            false
        }
    } else false
}

fun TextInputLayout.getTextIfVisible(editText: TextInputEditText): String = if (this.isVisible) {
    editText.text.toString()
} else {
    ""
}

fun AutoCompleteTextView.getTextInEnglish(
    context: Context,
    @ArrayRes arrayResId: Int
): String {
    val selectedText = this.text.toString()
    val localizedArray = context.resources.getStringArray(arrayResId)
    val typedArray = context.resources.obtainTypedArray(arrayResId)

    val index = localizedArray.indexOf(selectedText)
    if (index == -1) {
        typedArray.recycle()
        return ""
    }

    val resId = index.let { typedArray.getResourceId(it, 0) }
    typedArray.recycle()

    val englishResources = LanguageUtils.getSpecificLocalResource(context, "en")
    return englishResources.getString(resId)
}

fun AutoCompleteTextView.setItemFromString(
    context: Context,
    @ArrayRes arrayResId: Int,
    englishText: String
) {
    val localizedArray = context.resources.getStringArray(arrayResId)
    val index = localizedArray.indexOf(englishText)
    if (index != -1) {
        this.setText(localizedArray[index], false)
    }
}
