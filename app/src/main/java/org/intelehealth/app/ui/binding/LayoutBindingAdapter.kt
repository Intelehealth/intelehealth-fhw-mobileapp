package org.intelehealth.app.ui.binding

import android.widget.NumberPicker
import androidx.databinding.BindingAdapter
import com.github.ajalt.timberkt.Timber
import com.google.android.material.button.MaterialButtonToggleGroup
import org.intelehealth.app.R

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
    Timber.d { "maxNumber" }
    if (numberPicker != null && value != null) {
        numberPicker.maxValue = value
    } else Timber.d { "maxNumber else" }
}