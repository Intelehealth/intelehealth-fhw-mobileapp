package org.intelehealth.app.ui.binding

import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.button.MaterialButtonToggleGroup
import org.intelehealth.app.R
import org.intelehealth.app.models.IntroContent

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