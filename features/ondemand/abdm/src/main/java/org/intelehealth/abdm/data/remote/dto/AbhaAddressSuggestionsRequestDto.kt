package org.intelehealth.abdm.data.remote.dto

import com.google.gson.annotations.SerializedName

data class AbhaAddressSuggestionsRequestDto(
    @SerializedName("txnId") val txnId: String,
)