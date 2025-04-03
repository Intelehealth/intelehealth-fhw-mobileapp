package org.intelehealth.coreroomdb.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "tbl_image_records")
data class MediaRecord(
    @SerializedName("uuid")
    @PrimaryKey
    var uuid: String? = null,
    @SerializedName("patientuuid")
    var patientUuid: String? = null,
    @SerializedName("visituuid")
    var visitUuid: String? = null,
    @SerializedName("encounteruuid")
    var encounterUuid: String? = null,
    @SerializedName("image_path")
    var imagePath: String? = null,
    @SerializedName("obs_time_date")
    var obsTimeDate: String? = null,
    @SerializedName("image_type")
    var imageType: String? = null,
    @SerializedName("sync")
    var sync: Boolean? = null,
    @SerializedName("voided")
    var voided: Int = 0
) : Parcelable