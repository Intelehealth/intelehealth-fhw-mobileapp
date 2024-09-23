package org.intelehealth.abdm.data.model

import com.google.gson.annotations.SerializedName

data class VerifyMobileOtpResponseDto(
    @SerializedName("txnId")
    val txnId: String? = null,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("authResult")
    val authResult: String? = null
)