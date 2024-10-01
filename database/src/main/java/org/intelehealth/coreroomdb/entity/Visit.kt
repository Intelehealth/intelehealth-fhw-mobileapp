package org.intelehealth.coreroomdb.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "tbl_visit")
data class Visit(
    @PrimaryKey
    @SerializedName("uuid") var uuid: String,
    @ColumnInfo("patientuuid") @SerializedName("patientuuid") var patientUuid: String? = null,
    @ColumnInfo("visit_type_uuid") @SerializedName("visit_type_uuid") var visitTypeUuid: String? = null,
    @ColumnInfo("startdate") @SerializedName("startdate") var startDate: String? = null,
    @ColumnInfo("enddate") @SerializedName("enddate") var endDate: String? = null,
    @ColumnInfo("locationuuid") @SerializedName("locationuuid") var locationUuid: String? = null,
    @ColumnInfo("creator") @SerializedName("creator") var creatorUuid: String? = null,
    @SerializedName("sync") var sync: Boolean? = null,
    @ColumnInfo("modified_date") @SerializedName("modified_date") var modifiedDate: String? = null,
    @ColumnInfo("isdownloaded") @SerializedName("isdownloaded") var downloaded: Boolean = false,
    @SerializedName("voided") var voided: Int = 0,
    @ColumnInfo("issubmitted") @SerializedName("issubmitted") var submitted: Int = 0
) : Parcelable