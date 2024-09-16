package org.intelehealth.abdm.domain.model.request

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class SendAadhaarOtpApiRequest(
    @SerializedName("value")
    @Expose
    var value: String? = null,

    @SerializedName("scope")
    @Expose
    var scope: String? = null,

    @SerializedName("txnId")
    @Expose
    var txnId: String? = null
)
