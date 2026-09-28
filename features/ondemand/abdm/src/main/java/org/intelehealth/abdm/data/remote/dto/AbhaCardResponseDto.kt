package org.intelehealth.abdm.data.remote.dto

import com.google.gson.annotations.SerializedName

data class AbhaCardResponseDto(
    @SerializedName("image") val image: String?,
)