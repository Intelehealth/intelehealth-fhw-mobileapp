package org.intelehealth.coreroomdb.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "tbl_uuid_dictionary")
data class Concept(
    @PrimaryKey
    @SerializedName("uuid")
    var uuid: String? = null,
    @SerializedName("name")
    var name: String? = null
) : Parcelable
