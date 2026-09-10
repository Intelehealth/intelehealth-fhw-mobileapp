package org.intelehealth.abdm.presentation.abha_verify

import androidx.annotation.StringRes

/** One labelled field shown on the compare screen, with the HMIS (local) and ABHA values. */
internal data class CompareField(
    @StringRes val labelRes: Int,
    val localValue: String,
    val abhaValue: String,
)
