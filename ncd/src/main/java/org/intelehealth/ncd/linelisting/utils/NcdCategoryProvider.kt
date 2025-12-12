package org.intelehealth.ncd.linelisting.utils

import org.intelehealth.ncd.constants.Constants
import org.intelehealth.ncd.linelisting.NcdCategory

object NcdCategoryProvider {
    val ALL_CATEGORIES = listOf(
        NcdCategory(Constants.ANEMIA_SCREENING, "Anemia Screening", NcdCategory.Type.SCREENING),
        NcdCategory(Constants.ANEMIA_FOLLOW_UP, "Anemia Followup", NcdCategory.Type.FOLLOW_UP),
        NcdCategory(Constants.DIABETES_SCREENING, "Diabetes Screening", NcdCategory.Type.SCREENING),
        NcdCategory(Constants.DIABETES_FOLLOW_UP, "Diabetes Followup", NcdCategory.Type.FOLLOW_UP),
        NcdCategory(Constants.HYPERTENSION_SCREENING, "Hypertension Screening", NcdCategory.Type.SCREENING),
        NcdCategory(Constants.HYPERTENSION_FOLLOW_UP, "Hypertension Followup", NcdCategory.Type.FOLLOW_UP),
        NcdCategory(Constants.GENERAL, "General", NcdCategory.Type.GENERAL),
    )

    fun fromKey(key: String): NcdCategory? {
        return ALL_CATEGORIES.find { it.key == key }
    }
}