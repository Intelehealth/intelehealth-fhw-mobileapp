package org.intelehealth.abdm.data.remote.dto

import com.google.gson.annotations.SerializedName

data class RegisterNumberWithAbdmRequestDto(
    @SerializedName("otp") val otp: String,
    @SerializedName("txnId") val txnId: String,
    @SerializedName("mobileNo") val mobileNumber: String,
)
