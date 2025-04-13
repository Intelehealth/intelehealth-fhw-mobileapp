package org.intelehealth.app.utilities.extensions

import android.content.Context
import android.view.View
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import org.intelehealth.app.utilities.LanguageUtils.getSpecificLocalResource

fun ChipGroup.getSelectedChipTextInEnglishLocale(context: Context): String? {
    val chipId = checkedChipId
    if (chipId == View.NO_ID) return null

    val chip = findViewById<Chip>(chipId)
    val resId = chip.tag as? Int ?: return null

    val localizedRes = getSpecificLocalResource(context, "en")
    return localizedRes.getString(resId)
}

fun ChipGroup.checkChipBySelectedText(
    englishText: String,
    context: Context
) {
    val resources = getSpecificLocalResource(context, "en")

    for (i in 0 until childCount) {
        val chip = getChildAt(i) as? Chip ?: continue
        val resId = chip.tag as? Int ?: continue
        val localeString = resources.getString(resId)

        if (localeString.equals(englishText, ignoreCase = true)) {
            chip.isChecked = true
            break
        }
    }
}