package org.intelehealth.abdm.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SearchProfileRequestDto(
    @SerializedName("value") val value: String,
)
