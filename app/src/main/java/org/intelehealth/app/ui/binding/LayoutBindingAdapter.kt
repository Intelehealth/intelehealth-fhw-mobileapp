package org.intelehealth.app.ui.binding

import android.view.View
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

