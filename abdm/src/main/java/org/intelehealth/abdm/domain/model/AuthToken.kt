package org.intelehealth.abdm.domain.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class AuthToken(
    @SerializedName("accessToken")
    @Expose
    var accessToken: String? = null,

    @SerializedName("expiresIn")
    @Expose
    var expiresIn: Int? = null,

    @SerializedName("refreshExpiresIn")
    @Expose
    var refreshExpiresIn: Int? = null,

    @SerializedName("refreshToken")
    @Expose
    var refreshToken: String? = null,

    @SerializedName("tokenType")
    @Expose
    var tokenType: String? = null
)