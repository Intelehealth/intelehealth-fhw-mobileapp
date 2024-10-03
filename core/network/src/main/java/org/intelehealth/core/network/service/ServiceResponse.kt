package org.intelehealth.core.network.service

import com.google.gson.annotations.SerializedName

class ServiceResponse<T>(
    @SerializedName("status") val status: Int = 500,
    @SerializedName("data") val data: T? = null,
    @SerializedName("message") val message: String? = null
)