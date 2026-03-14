package org.intelehealth.abdm.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.intelehealth.abdm.enums.AbdmOutcomes

@Parcelize
data class AbdmResult(
    val outcome: AbdmOutcomes,
    val accessToken: String?,
    val patientDetail: Boolean?,
    val firstRequestFulfilled: Boolean?,
    val otpVerificationResponse: OTPVerificationResponse
) : Parcelable
