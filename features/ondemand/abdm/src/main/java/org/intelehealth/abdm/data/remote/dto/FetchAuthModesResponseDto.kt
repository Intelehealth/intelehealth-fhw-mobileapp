package org.intelehealth.abdm.data.remote.dto

import com.google.gson.annotations.SerializedName

data class FetchAuthModesResponseDto(
    @SerializedName("healthIdNumber") val healthIdNumber: String?,
    @SerializedName("abhaAddress") val abhaAddress: String?,
    @SerializedName("authMethods") val authMethods: List<String>?,
    @SerializedName("blockedAuthMethods") val blockedAuthMethods: List<String>?,
    @SerializedName("status") val status: String?,
    @SerializedName("message") val message: String?,
    @SerializedName("fullName") val fullName: String?,
    @SerializedName("mobile") val mobile: String?,
)