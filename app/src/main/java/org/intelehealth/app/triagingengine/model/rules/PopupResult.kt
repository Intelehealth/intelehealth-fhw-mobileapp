package org.intelehealth.app.triagingengine.model.rules

import com.google.gson.annotations.SerializedName

/**
 * Created by Lincon Pradhan on  03-02-2025.
 **/
data class PopupResult(
    @SerializedName("key") val key: String,
    @SerializedName("condition") val condition: String,
    @SerializedName("popup") val popup: String,
    @SerializedName("popup-hi") val popupHi: String
)