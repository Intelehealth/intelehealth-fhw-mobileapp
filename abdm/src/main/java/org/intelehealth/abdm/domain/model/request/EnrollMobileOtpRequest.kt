package org.intelehealth.abdm.domain.model.request

import com.google.gson.annotations.SerializedName

data class EnrollMobileOtpRequest(
    @SerializedName("otp") var otp: String,
    @SerializedName("txnId") var txnId: String,
    @SerializedName("mobileNo") var mobileNo: String
)