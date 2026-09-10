package org.intelehealth.abdm.data.remote.dto

import com.google.gson.annotations.SerializedName

data class RegisterAbhaAddressRequestDto(
    @SerializedName("txnId") val txnId: String,
    @SerializedName("abhaAddress") val abhaAddress: String,
)
