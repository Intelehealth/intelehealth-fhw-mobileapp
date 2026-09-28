package org.intelehealth.abdm.data.remote.dto

import com.google.gson.annotations.SerializedName

data class AbhaAccountDto(
    @SerializedName("ABHANumber") val abhaNumber: String?,
    @SerializedName("preferredAbhaAddress") val preferredAbhaAddress: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("gender") val gender: String?,
    @SerializedName("dob") val dob: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("profilePhoto") val profilePhoto: String?,
    @SerializedName("kycVerified") val kycVerified: Boolean?,
    @SerializedName("index") val index: Int?,
)