package org.intelehealth.abdm.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UpdateIdentifierRequestDto(
    @SerializedName("identifier") val identifier: String,
    @SerializedName("identifierType") val identifierType: String,
    @SerializedName("location") val location: String,
)
