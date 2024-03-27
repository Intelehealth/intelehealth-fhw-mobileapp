package org.intelehealth.config.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Created by Vaghela Mithun R. on 15-03-2024 - 16:01.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
@Entity(tableName = "config_dictionary")
data class ConfigDictionary(
    @PrimaryKey
    @SerializedName("id")
    val configId: Int,
    @SerializedName("key")
    val dicKey: String,
    @SerializedName("value")
    val configValue: String = "",
    val type: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("default_value")
    val defaultValue: String,
)
