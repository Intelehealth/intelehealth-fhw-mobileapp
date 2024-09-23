package org.intelehealth.abdm.data.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class SendMobileOtpDto(
    @SerializedName("txnId") @Expose
    var txnId: String? = null,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("authResult")
    val authResult: String? = null
)
