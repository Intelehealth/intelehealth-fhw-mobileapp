package org.intelehealth.abdm.domain.model

import com.google.gson.annotations.SerializedName

data class EnrollMobileOtpResponseData(
    @SerializedName("txnId")
    private val txnId: String? = null,

    @SerializedName("message")
    private val message: String? = null,

    @SerializedName("authResult")
    private val authResult: String? = null
)