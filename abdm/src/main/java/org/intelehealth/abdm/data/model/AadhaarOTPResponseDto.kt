package org.intelehealth.abdm.data.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class AadhaarOTPResponseDto(
    @SerializedName("txnId")
    @Expose
    var txnId: String? = null,

    @SerializedName("message")
    @Expose
    var message: String? = null,

    @SerializedName("authResult")
    @Expose
    var authResult: String? = null
)