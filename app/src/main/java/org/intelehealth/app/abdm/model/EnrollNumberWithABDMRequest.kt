package org.intelehealth.app.abdm.model

import com.google.gson.annotations.SerializedName


data class EnrollNumberWithABDMRequest(
    @SerializedName("otp") var otp: String? = null,
    @SerializedName("txnId") var txnId: String? = null,
    @SerializedName("mobileNo") var mobileNo: String? = null
)