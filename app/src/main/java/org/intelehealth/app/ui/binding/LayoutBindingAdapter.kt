package org.intelehealth.app.ui.binding

import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.annotation.DimenRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.forEach
import androidx.core.view.marginStart
import androidx.databinding.BindingAdapter
import com.github.ajalt.timberkt.Timber
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.textfield.TextInputEditText
import org.intelehealth.app.R
import org.intelehealth.app.utilities.LanguageUtils
import org.intelehealth.app.utilities.extensions.setSelectedCheckboxes
import org.intelehealth.config.network.response.PatientRegFieldConfig
import org.intelehealth.config.room.entity.PatientRegistrationFields
import java.util.Locale

/**
 * Created by Vaghela Mithun R. on 11-07-2024 - 19:55.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/

@BindingAdapter("gender")
fun genderViewBinding(btnToggleGroup: MaterialButtonToggleGroup?, gender: String?) {
    if (btnToggleGroup != null && gender != null) {
        if (gender.equals("M", ignoreCase = true)) btnToggleGroup.check(R.id.btnMale)
        else if (gender.equals("F", ignoreCase = true)) btnToggleGroup.check(R.id.btnFemale)
        else btnToggleGroup.check(R.id.btnOther)
    }
}

@BindingAdapter("minNumber")
fun bindMinValue(numberPicker: NumberPicker?, value: Int?) {
    if (numberPicker != null && value != null) {
        numberPicker.minValue = value
    }
}

@BindingAdapter("maxNumber")
fun bindMaxValue(numberPicker: NumberPicker?, value: Int?) {
    if (numberPicker != null && value != null) {
        numberPicker.maxValue = value
    }
}

@BindingAdapter(value = ["config", "editMode"], requireAll = true)
fun changeEditMode(view: View?, config: PatientRegistrationFields?, editMode: Boolean) {
    if (view != null && config != null) {
        view.isEnabled = (!config.isEditable && editMode).not()
    }
}

@BindingAdapter(value = ["config", "dynamicMargin"], requireAll = true)
fun maintainDynamicMargin(view: View?, config: PatientRegistrationFields?, margin: Float) {
    if (view != null && config != null) {
        val param = view.layoutParams as ConstraintLayout.LayoutParams
        param.marginStart = if (config.isEnabled) margin.toInt() else 0
        view.layoutParams = param
    }
}

@BindingAdapter("radioButtonValue")
fun bindRadioButtonValue(radioGroup: RadioGroup?, data: String?) {
    radioGroup?.forEach { radioButton ->
        data?.let { text ->
            if (radioButton is RadioButton) {
                val englishLocaleResources = LanguageUtils.getSpecificLocalResource(
                    radioButton.context!!,
                    "en"
                )

                val tag: Int = radioButton.tag as Int
                val englishText = englishLocaleResources.getString(tag)
                if (englishText == text) {
                    radioButton.isChecked = true
                }
            }
        }
    }
}

@BindingAdapter("bindAutoCompleteValue", "stringArrayResId")
fun bindAutoCompleteValue(
    autoCompleteTextView: AutoCompleteTextView?,
    data: String?,
    stringArrayResId: Int
) {
    autoCompleteTextView?.let { view ->
        data?.let { data ->
            val englishResources = LanguageUtils.getSpecificLocalResource(view.context, "en")
            val englishArray = englishResources.getStringArray(stringArrayResId)
            val index = englishArray.indexOf(data)

            if (index != -1) {
                val localizedResources = LanguageUtils.getSpecificLocalResource(
                    view.context,
                    Locale.getDefault().language
                )

                val localizedArray = localizedResources.getStringArray(stringArrayResId)
                val localizedValue = localizedArray[index]
                view.setText(localizedValue, false)
            } else {
                view.setText(data, false)
            }
        }
    }
}

@BindingAdapter("bindCheckBoxesValue")
fun bindCheckBoxesValue(linearLayout: LinearLayout, data: String) {
    linearLayout.setSelectedCheckboxes(data)
}
@BindingAdapter("bindRadioButtonValueNew")
fun bindRadioButtonValueNew(radioGroup: RadioGroup?, data: String?) {
    if (radioGroup == null) return

    if (data.isNullOrEmpty() || data == "-") {
        // No default selection
        radioGroup.clearCheck()
        return
    }

    radioGroup.forEach { view ->
        if (view is RadioButton) {
            val englishLocaleResources = LanguageUtils.getSpecificLocalResource(
                view.context,
                "en"
            )
            val tag: Int = view.tag as Int
            val englishText = englishLocaleResources.getString(tag)

            if (englishText == data) {
                view.isChecked = true
            }
        }
    }
    @BindingAdapter(
        value = ["bindAutoCompleteForOtherReason", "stringArrayResId", "otherReasonView"],
        requireAll = false
    )
    fun bindAutoCompleteForOtherReason(
        autoComplete: AutoCompleteTextView,
        value: String?,
        stringArrayResId: Int?,
        etOther: TextInputEditText?
    ) {
        // Dropdown setup
        stringArrayResId?.let {
            val items = autoComplete.context.resources.getStringArray(it).toList()
            val adapter = ArrayAdapter(autoComplete.context, android.R.layout.simple_dropdown_item_1line, items)
            autoComplete.setAdapter(adapter)
        }

        // Handle DB/ViewModel value
        if (value.isNullOrEmpty()) {
            autoComplete.setText("", false)
            etOther?.visibility = View.GONE
            etOther?.setText("")
            return
        }

        if (value.contains(":reason ")) {
            val parts = value.split(":reason ")
            if (parts.size == 2) {
                autoComplete.setText(parts[0], false) // e.g. "Unknown / Other"
                etOther?.visibility = View.VISIBLE
                etOther?.setText(parts[1])            // e.g. "kz gdhd"
            }
        } else {
            autoComplete.setText(value, false)       // normal option
            etOther?.visibility = View.GONE
            etOther?.setText("")
        }
    }

}

