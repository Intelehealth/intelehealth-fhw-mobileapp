package org.intelehealth.abdm.domain.model.request

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


data class AadhaarOtpVerificationRequest(
    @SerializedName("otp")
    @Expose
    var otp: String? = null,

    @SerializedName("txnId")
    @Expose
    var txnId: String? = null,

    @SerializedName("mobileNo")
    @Expose
    var mobileNo: String? = null,

)