package org.intelehealth.abdm.domain.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Tokens(
    @SerializedName("token")
    @Expose
    var token: String? = null,

    @SerializedName("expiresIn")
    @Expose
    var expiresIn: Int? = null,

    @SerializedName("refreshToken")
    @Expose
    var refreshToken: String? = null,

    @SerializedName("refreshExpiresIn")
    @Expose
    var refreshExpiresIn: Int? = null
)
