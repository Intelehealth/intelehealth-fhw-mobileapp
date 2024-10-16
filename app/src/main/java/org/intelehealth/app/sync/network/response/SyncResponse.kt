package org.intelehealth.app.sync.network.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import org.intelehealth.app.models.dto.Data

data class SyncResponse(

    @SerializedName("status")
    @Expose
    val status: String? = null,

    @SerializedName("data")
    @Expose
    val data: Data? = null

)
