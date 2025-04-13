package org.intelehealth.app.utilities.extensions

import android.content.Context
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import org.intelehealth.app.utilities.LanguageUtils

fun RadioGroup.validate(): Boolean {
    return checkedRadioButtonId != -1
}

fun RadioGroup.getSelectedData(): String {
    return if (checkedRadioButtonId != -1) {
        val checkedRadioButton = findViewById<RadioButton>(checkedRadioButtonId)
        checkedRadioButton.text.toString()
    } else ""
}

fun RadioGroup.setCheckedRadioButtonByText(targetText: String) {
    for (i in 0 until childCount) {
        val child = getChildAt(i)
        if (child is RadioButton && child.text.toString() == targetText) {
            child.isChecked = true
            break
        }
    }
}

fun RadioGroup.getSelectedDataInEnglishLocale(context: Context): String {
    val checkedId = checkedRadioButtonId
    if (checkedId == View.NO_ID) return ""

    val radioButton = findViewById<RadioButton>(checkedId)
    val resId = radioButton.tag as? Int ?: return ""

    val englishResources = LanguageUtils.getSpecificLocalResource(context, "en")
    return englishResources.getString(resId)
}