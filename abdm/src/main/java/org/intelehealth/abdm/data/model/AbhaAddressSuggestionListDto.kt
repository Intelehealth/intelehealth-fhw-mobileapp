package org.intelehealth.abdm.data.model

import com.google.gson.annotations.SerializedName

data class AbhaAddressSuggestionListDto(
    @SerializedName("txnId")
    var txnId: String? = null,
    @SerializedName("abhaAddressList")
    var abhaAddressList: List<String>? = null
)