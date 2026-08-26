package org.intelehealth.abdm.data.remote.dto

import com.google.gson.annotations.SerializedName

data class RegisterAbhaAddressResponseDto(
    @SerializedName("message") val message: String?,
    @SerializedName("txnId") val txnId: String?,
    @SerializedName("healthIdNumber") val healthIdNumber: String?,
    @SerializedName("preferredAbhaAddress") val preferredAbhaAddress: String?,
)
