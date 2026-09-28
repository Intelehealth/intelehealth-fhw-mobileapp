package org.intelehealth.abdm.data.remote.dto

import com.google.gson.annotations.SerializedName

data class VerifyOtpRequestDto(
    @SerializedName("otp") val otp: String,
    @SerializedName("txnId") val txnId: String,
    @SerializedName("mobileNo") val mobileNumber: String? = null,
    @SerializedName("scope") val scope: String? = null,
    @SerializedName("authMethod") val authMethod: String? = null,
)
