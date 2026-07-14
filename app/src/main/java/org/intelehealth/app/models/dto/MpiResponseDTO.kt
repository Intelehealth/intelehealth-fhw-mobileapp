package org.intelehealth.app.models.dto

import com.google.gson.annotations.SerializedName

data class MpiResponseDTO(
    @SerializedName("status")
    val status: String,

    @SerializedName("data")
    val data: MpiDataDTO? = null,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("label")
    val label: String? = null
)

data class MpiDataDTO(

    @SerializedName("mpi")
    val mpi: String? = null,

    @SerializedName("attempt_number")
    val attemptNumber: Int? = null,

    @SerializedName("last_try")
    val lastTry: String? = null,

    @SerializedName("status")
    val status: String? = null
)