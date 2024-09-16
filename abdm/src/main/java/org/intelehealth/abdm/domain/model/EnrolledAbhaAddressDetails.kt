package org.intelehealth.abdm.domain.model

import com.google.gson.annotations.SerializedName

class EnrolledAbhaAddressDetails(
    @SerializedName("message")
    var message: String? = null,

    @SerializedName("txnId")
    var txnId: String? = null,

    @SerializedName("healthIdNumber")
    var healthIdNumber: String? = null,

    @SerializedName("preferredAbhaAddress")
    var preferredAbhaAddress: String? = null,
)