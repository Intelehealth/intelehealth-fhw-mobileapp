package org.intelehealth.app.ui.binding

import android.view.View
import android.widget.NumberPicker
import androidx.annotation.DimenRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.marginStart
import androidx.databinding.BindingAdapter
import com.github.ajalt.timberkt.Timber
import com.google.android.material.button.MaterialButtonToggleGroup
import org.intelehealth.app.R
import org.intelehealth.config.network.response.PatientRegFieldConfig
import org.intelehealth.config.room.entity.PatientRegistrationFields

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
fun maintainDynamicMargin(view: View?, config: PatientRegistrationFields?,  margin: Float) {
    if (view != null && config != null) {
        val param = view.layoutParams as ConstraintLayout.LayoutParams
        param.marginStart = if (config.isEnabled) margin.toInt() else 0
        view.layoutParams = param
    }
}

