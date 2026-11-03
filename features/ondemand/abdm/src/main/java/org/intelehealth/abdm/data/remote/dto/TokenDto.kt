package org.intelehealth.abdm.data.remote.dto

import com.google.gson.annotations.SerializedName

data class TokenDto(
    @SerializedName("accessToken") val accessToken: String?,
    @SerializedName("expiresIn") val expiresIn: Int?,
    @SerializedName("refreshExpiresIn") val refreshExpiresIn: Int?,
    @SerializedName("refreshToken") val refreshToken: String?,
    @SerializedName("tokenType") val tokenType: String?,
)