package org.intelehealth.abdm.domain.model.request

import com.google.gson.annotations.SerializedName

data class AbhaAddressSuggestionRequest(
    @SerializedName("txnId")
    private val txnId: String? = null
)
