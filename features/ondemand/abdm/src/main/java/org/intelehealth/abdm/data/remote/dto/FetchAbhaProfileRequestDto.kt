package org.intelehealth.abdm.data.remote.dto

import com.google.gson.annotations.SerializedName

data class FetchAbhaProfileRequestDto(
    @SerializedName("txnId") val txnId: String,
    @SerializedName("abhaNumber") val abhaNumber: String? = null,
    @SerializedName("scope") val scope: String,
)