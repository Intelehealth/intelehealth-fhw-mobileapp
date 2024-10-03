package org.intelehealth.config.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Created by Vaghela Mithun R. on 29-05-2024 - 17:11.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
@Entity(tableName = "tbl_patient_vital")
data class PatientVital(
    val name: String,

    @SerializedName("key")

    @PrimaryKey
    val vitalKey: String,

    val uuid: String,

    @SerializedName("is_mandatory")
    val isMandatory: Boolean,

    /*@SerializedName("is_enabled")
    val isEnabled: Boolean*/
)
