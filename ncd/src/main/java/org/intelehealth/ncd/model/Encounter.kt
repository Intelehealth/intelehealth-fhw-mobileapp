package org.intelehealth.ncd.model

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import org.intelehealth.ncd.entity.BaseEntity

@Entity(tableName = "tbl_encounter")
data class Encounter(
    @PrimaryKey
    @ColumnInfo(name = "uuid")
    val uuid: String,

    @ColumnInfo(name = "visituuid")
    val visituuid: String? = null,

    @ColumnInfo(name = "encounter_time")
    val encounter_time: String? = null,

    @ColumnInfo(name = "provider_uuid")
    val provider_uuid: String? = null,

    @ColumnInfo(name = "encounter_type_uuid")
    val encounter_type_uuid: String? = null,

    @ColumnInfo(name = "modified_date")
    val modified_date: String? = null,

    @ColumnInfo(name = "sync", defaultValue = "'false'")
    val sync: String? = null,

    @ColumnInfo(name = "voided", defaultValue = "'0'")
    val voided: String? = null,

    @ColumnInfo(name = "privacynotice_value")
    val privacynotice_value: String? = null
)
