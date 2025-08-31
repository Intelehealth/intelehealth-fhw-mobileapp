package org.intelehealth.app.activities.user.api

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class UserSessionRequest(
    @SerializedName("value")
    val value: String,  // Must be a String, not List
    @SerializedName("attributeType")
    val attributeType: String
)
