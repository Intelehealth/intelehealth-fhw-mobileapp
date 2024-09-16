package org.intelehealth.abdm.data.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable


class AadhaarOtpVerificationResponseDto : Serializable {
    @SerializedName("message")
    @Expose
    var message: String? = null

    @SerializedName("txnId")
    @Expose
    var txnId: String? = null

    @SerializedName("tokens")
    @Expose
    var tokens: TokensDto? = null

    @SerializedName("ABHAProfile")
    @Expose
    var abhaProfile: ABHAProfileDto? = null

    @SerializedName("isNew")
    @Expose
    var isNew: Boolean? = null
    var uuID: String? = null
    var openMrsId: String? = null



}
