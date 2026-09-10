package org.intelehealth.abdm.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SearchProfileResponseDto(
    @SerializedName("txnId") val txnId: String?,
    @SerializedName("ABHA") val abhaAccount: List<AbhaAccountDto>?,
)