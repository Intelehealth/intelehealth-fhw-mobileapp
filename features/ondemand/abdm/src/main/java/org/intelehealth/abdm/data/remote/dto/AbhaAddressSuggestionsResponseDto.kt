package org.intelehealth.abdm.data.remote.dto

import com.google.gson.annotations.SerializedName

data class AbhaAddressSuggestionsResponseDto(
    @SerializedName("txnId") val txnId: String?,
    @SerializedName("abhaAddressList") val abhaAddressList: List<String>?,
)
