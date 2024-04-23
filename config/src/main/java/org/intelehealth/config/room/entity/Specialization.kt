package org.intelehealth.config.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Created by Vaghela Mithun R. on 10-04-2024 - 17:23.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
@Entity(tableName = "tbl_specialization")
data class Specialization(
    @PrimaryKey
    @SerializedName("key")
    val sKey: String,
    @SerializedName("name")
    val name: String
)
