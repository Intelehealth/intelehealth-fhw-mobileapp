package org.intelehealth.abdm.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.intelehealth.abdm.enums.AbdmOutcomes

@Parcelize
data class AbdmResult(
    val outcome: AbdmOutcomes,
    val accessToken: String?,
    val xToken: String?,
    val otpResponse: OTPVerificationResponse?,
    val abhaResponse: AbhaProfileResponse?
) : Parcelable
