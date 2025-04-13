package org.intelehealth.app.utilities.extensions

import android.view.View
import android.widget.CheckBox
import android.widget.LinearLayout
import androidx.core.view.isVisible
import com.google.android.material.textfield.TextInputEditText
import org.intelehealth.app.utilities.LanguageUtils
import org.json.JSONArray

fun LinearLayout.validateCheckboxes(): Boolean {
    var isSelected = false
    for (i in 0 until childCount) {
        val currentView: View = getChildAt(i)
        if (currentView is CheckBox && currentView.isChecked) {
            isSelected = true
            break
        }
    }

    return isSelected
}

fun LinearLayout.getTextIfVisible(editText: TextInputEditText): String = if (this.isVisible) {
    editText.text.toString()
} else {
    ""
}

fun LinearLayout.getSelectedCheckboxes(): String {
    val result = JSONArray()
    val englishResources = LanguageUtils.getSpecificLocalResource(context, "en")

    for (i in 0 until this.childCount) {
        val child = this.getChildAt(i)
        if (child is CheckBox && child.isChecked) {
            val resId = child.tag as? Int
            if (resId != null) {
                val englishText = englishResources.getString(resId)
                result.put(englishText)
            }
        }
    }

    return result.toString()
}


fun LinearLayout.setSelectedCheckboxes(data: String) {
    val normalizedData = data.replace("\\/", "/")
    val englishResources = LanguageUtils.getSpecificLocalResource(context, "en")

    for (i in 0 until this.childCount) {
        val child = this.getChildAt(i)
        if (child is CheckBox) {
            val resId = child.tag as? Int
            if (resId != null) {
                val englishText = englishResources.getString(resId)
                child.isChecked = normalizedData.contains(englishText)
            }
        }
    }
}