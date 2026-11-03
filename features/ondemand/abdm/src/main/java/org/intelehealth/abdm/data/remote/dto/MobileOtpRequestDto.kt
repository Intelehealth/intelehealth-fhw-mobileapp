package org.intelehealth.abdm.data.remote.dto

import com.google.gson.annotations.SerializedName

data class MobileOtpRequestDto(
    @SerializedName("value") val value: String,
    @SerializedName("scope") val scope: String,
    @SerializedName("authMethod") val authMethod: String? = null,
    @SerializedName("txnId") val txnId: String? = null,
)
