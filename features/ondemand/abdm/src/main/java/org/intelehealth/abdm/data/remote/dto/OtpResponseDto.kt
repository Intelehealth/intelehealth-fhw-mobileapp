package org.intelehealth.abdm.data.remote.dto

import com.google.gson.annotations.SerializedName

data class OtpResponseDto(
    @SerializedName("txnId") val txnId: String?,
    @SerializedName("message") val message: String?,
    @SerializedName("authResult") val authResult: String?,
)