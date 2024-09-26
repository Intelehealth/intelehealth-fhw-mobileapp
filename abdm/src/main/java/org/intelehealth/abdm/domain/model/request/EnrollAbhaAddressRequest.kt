package org.intelehealth.abdm.domain.model.request

import com.google.gson.annotations.SerializedName

data class EnrollAbhaAddressRequest(
    @SerializedName("txnId")
    private val txnId: String,
    @SerializedName("abhaAddress")
    private val abhaAddress: String,
)
