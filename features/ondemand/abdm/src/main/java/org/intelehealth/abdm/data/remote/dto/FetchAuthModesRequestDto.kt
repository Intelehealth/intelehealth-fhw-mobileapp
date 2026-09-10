package org.intelehealth.abdm.data.remote.dto

import com.google.gson.annotations.SerializedName

data class FetchAuthModesRequestDto(
    @SerializedName("abhaAddress") val abhaAddress: String,
)
