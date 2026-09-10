package org.intelehealth.abdm.data.remote.dto

import com.google.gson.annotations.SerializedName

data class VerifyOtpResponseDto(
    @SerializedName("message") val message: String?,
    @SerializedName("txnId") val txnId: String?,
    @SerializedName("tokens") val tokens: TokensDto?,
    @SerializedName("ABHAProfile") val abhaProfile: AbhaProfileDto?,
    @SerializedName("isNew") val isNew: Boolean?,
)

data class TokensDto(
    @SerializedName("token") val token: String?,
    @SerializedName("expiresIn") val expiresIn: Int?,
    @SerializedName("refreshToken") val refreshToken: String?,
    @SerializedName("refreshExpiresIn") val refreshExpiresIn: Int?,
)

data class AbhaProfileDto(
    @SerializedName("firstName") val firstName: String?,
    @SerializedName("middleName") val middleName: String?,
    @SerializedName("lastName") val lastName: String?,
    @SerializedName("dob") val dob: String?,
    @SerializedName("gender") val gender: String?,
    @SerializedName("photo") val photo: String?,
    @SerializedName("mobile") val mobile: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("phrAddress") val phrAddress: List<String>?,
    @SerializedName("address") val address: String?,
    @SerializedName("districtCode") val districtCode: String?,
    @SerializedName("stateCode") val stateCode: String?,
    @SerializedName("pinCode") val pinCode: String?,
    @SerializedName("abhaType") val abhaType: String?,
    @SerializedName("stateName") val stateName: String?,
    @SerializedName("districtName") val districtName: String?,
    @SerializedName("ABHANumber") val abhaNumber: String?,
    @SerializedName("abhaStatus") val abhaStatus: String?,
    @SerializedName("preferredAddress") val preferredAddress: String?,
)
