package org.intelehealth.app.models

import com.google.gson.annotations.SerializedName

data class SpecialtyNote(
    @SerializedName("specialty")
    val specialty: String,
    @SerializedName("notes")
    val notes: List<String>,
    @SerializedName("is_enabled")
    val isEnabled: Int
)
