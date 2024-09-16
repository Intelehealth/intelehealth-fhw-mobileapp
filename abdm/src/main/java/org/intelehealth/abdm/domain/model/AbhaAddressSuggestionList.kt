package org.intelehealth.abdm.domain.model

import com.google.gson.annotations.SerializedName

data class AbhaAddressSuggestionList(
    @SerializedName("txnId")
    var txnId: String? = null,
    @SerializedName("abhaAddressList")
    var abhaAddressList: List<String>? = null
)