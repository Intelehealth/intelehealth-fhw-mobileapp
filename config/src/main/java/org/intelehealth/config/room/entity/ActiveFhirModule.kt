package org.intelehealth.config.room.entity

import com.google.gson.annotations.SerializedName

data class ActiveFhirModule(
    @SerializedName("fhir")
    val fhir: Boolean = false
)