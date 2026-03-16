package org.intelehealth.config.room.entity

import com.google.gson.annotations.SerializedName

/**
 * Created by Lincon Pradhan
 * Email : lincon@intelehealth.org
 **/
data class SubSection(
    val name: String,
    @SerializedName("is_enabled")
    val isEnabled: Boolean
)