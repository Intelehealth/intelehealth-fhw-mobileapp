package org.intelehealth.abdm.domain.model.request

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class SendMobileOtpRequest(
    @SerializedName("value") @Expose
    private var value: String,
    @SerializedName("scope")
    private val scope: String,
    @SerializedName("txnId")
    private var txnId: String
)