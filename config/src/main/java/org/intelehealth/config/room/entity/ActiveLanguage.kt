package org.intelehealth.config.room.entity

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Created by Vaghela Mithun R. on 10-04-2024 - 17:28.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
@Entity(tableName = "tbl_language")
data class ActiveLanguage(
    val name: String,
    @PrimaryKey
    val code: String,
    @SerializedName("en_name")
    val generalName: String,
    @SerializedName("is_default")
    val isDefault: Boolean,
) {
    @Ignore
    var selected: Boolean = isDefault
}
