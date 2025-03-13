package org.intelehealth.core.network.service

import com.google.gson.annotations.SerializedName

class ServiceResponse<S, R>(
    @SerializedName("status") val status: S? = null,
    @SerializedName("data") val data: R? = null,
    @SerializedName("message") val message: String? = null
)