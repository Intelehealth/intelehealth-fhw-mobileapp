package org.intelehealth.app.abdm.model

import com.google.gson.annotations.SerializedName

class SearchAbhaProfileResponse(
    @SerializedName("txnId")
    var txnId: String? = null,
    @SerializedName("ABHA")
    var ABHA: ArrayList<Account> = arrayListOf(),
)
