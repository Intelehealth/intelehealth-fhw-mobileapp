package org.intelehealth.abdm.data.remote.dto

import com.google.gson.annotations.SerializedName

data class VerifyMobileOtpResponseDto(
    @SerializedName("txnId") val txnId: String?,
    @SerializedName("authResult") val authResult: String?,
    @SerializedName("message") val message: String?,
    @SerializedName("token") val token: String?,
    @SerializedName("refreshToken") val refreshToken: String?,
    @SerializedName("expiresIn") val expiresIn: Int?,
    @SerializedName("accounts") val accounts: List<AbhaAccountDto>?,
    @SerializedName("users") val users: List<AbhaUserDto>?,
)

data class AbhaUserDto(
    @SerializedName("abhaAddress") val abhaAddress: String?,
    @SerializedName("fullName") val fullName: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("kycStatus") val kycStatus: String?,
    @SerializedName("age") val age: Int?,
)
