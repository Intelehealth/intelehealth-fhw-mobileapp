package org.intelehealth.app.abdm.model

import com.google.gson.annotations.SerializedName

data class ExistUserStatusResponse(

    @SerializedName("status") var status: String? = null,
    @SerializedName("data") var data: Data? = Data()

)

data class Data(

    @SerializedName("uuid") var uuid: String? = null,
    @SerializedName("openmrsid") var openmrsid: String? = null

)