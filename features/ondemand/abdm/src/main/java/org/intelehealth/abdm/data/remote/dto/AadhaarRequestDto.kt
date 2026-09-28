package org.intelehealth.abdm.data.remote.dto

import com.google.gson.annotations.SerializedName

data class AadhaarRequestDto(
    @SerializedName("value") val value: String,
    @SerializedName("scope") val scope: String,
    @SerializedName("txnId") val txnId: String? = null,
)
