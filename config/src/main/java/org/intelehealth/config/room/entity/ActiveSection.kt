package org.intelehealth.config.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Created by Lincon Pradhan
 * Email : lincon@intelehealth.org
 **/
@Entity(tableName = "tbl_active_section")
data class ActiveSection
    (
    val name: String,
    @PrimaryKey
    val key: String,
    val lang: Map<String, String>,
    val label: String? = null,
    @SerializedName("is_enabled")
    val isEnable: Boolean,
    val order: Int,
    @SerializedName("sub_sections")
    val subSections: List<SubSection>? = null
)