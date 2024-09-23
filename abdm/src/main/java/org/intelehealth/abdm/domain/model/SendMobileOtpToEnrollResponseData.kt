package org.intelehealth.abdm.domain.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class SendMobileOtpToEnrollResponseData(
    @SerializedName("txnId") @Expose
    var txnId: String? = null,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("authResult")
    val authResult: String? = null
)
