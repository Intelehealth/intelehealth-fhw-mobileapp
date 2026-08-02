package org.intelehealth.abdm.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UserStatusResponseDto(
    @SerializedName("status") val status: String?,
    @SerializedName("data") val data: UserStatusDataDto?,
)

data class UserStatusDataDto(
    @SerializedName("uuid") val uuid: String?,
    @SerializedName("openmrsid") val openMrsId: String?,
)
