package org.intelehealth.app.abdm.model

import com.google.gson.annotations.SerializedName

data class ABDMErrorModel(

    @SerializedName("success") var success: Boolean? = null,
    @SerializedName("code") var code: String? = null,
    @SerializedName("message") var message: String? = null
)