package org.intelehealth.coreroomdb.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "tbl_image_records")
data class MediaRecord(
    @PrimaryKey
    @SerializedName("uuid") var uuid: String,
    @ColumnInfo("patientuuid") @SerializedName("patientuuid") var patientUuid: String? = null,
    @ColumnInfo("visituuid") @SerializedName("visituuid") var visitUuid: String? = null,
    @ColumnInfo("encounteruuid") @SerializedName("encounteruuid") var encounterUuid: String? = null,
    @ColumnInfo("image_path") @SerializedName("image_path") var imagePath: String? = null,
    @ColumnInfo("obs_time_date") @SerializedName("obs_time_date") var obsTimeDate: String? = null,
    @ColumnInfo("image_type") @SerializedName("image_type") var imageType: String? = null,
    @SerializedName("sync") var sync: Boolean? = null,
    @SerializedName("voided") var voided: Int = 0
) : Parcelable