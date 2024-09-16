package org.intelehealth.abdm.domain.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable


class AadhaarOtpVerification : Serializable {
    @SerializedName("message")
    @Expose
    var message: String? = null

    @SerializedName("txnId")
    @Expose
    var txnId: String? = null

    @SerializedName("tokens")
    @Expose
      var tokens: Tokens? = null

    @SerializedName("ABHAProfile")
    @Expose
      var abhaProfile: ABHAProfile? = null

    @SerializedName("isNew")
    @Expose
    var isNew: Boolean = false
    var uuID: String? = null
    var openMrsId: String? = null

}
