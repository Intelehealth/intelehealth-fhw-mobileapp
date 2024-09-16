package org.intelehealth.abdm.data.model

import com.google.gson.annotations.SerializedName

class EnrolledAbhaAddressDto(
    @SerializedName("message")
    var message: String? = null,

    @SerializedName("txnId")
    var txnId: String? = null,

    @SerializedName("healthIdNumber")
    var healthIdNumber: String? = null,

    @SerializedName("preferredAbhaAddress")
    var preferredAbhaAddress: String? = null,
)