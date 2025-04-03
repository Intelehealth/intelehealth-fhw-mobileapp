package org.intelehealth.coreroomdb.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "tbl_visit")
data class Visit(
    @PrimaryKey
    @SerializedName("uuid")
    var uuid: String? = null,
    @SerializedName("patientuuid")
    private var patientUuid: String? = null,
    @SerializedName("visit_type_uuid")
    var visitTypeUuid: String? = null,
    @SerializedName("startdate")
    var startDate: String? = null,
    @SerializedName("enddate")
    var endDate: String? = null,
    @SerializedName("locationuuid")
    var locationUuid: String? = null,
    @SerializedName("creator")
    var creatorUuid: String? = null,
    @SerializedName("sync")
    var sync: Boolean? = null,
    @SerializedName("modified_date")
    var modifiedDate: String? = null,
    @SerializedName("isdownloaded")
    var downloaded: Boolean = false,
    @SerializedName("voided")
    var voided: Int = 0,
    @SerializedName("issubmitted")
    var submitted: Int = 0
) : Parcelable