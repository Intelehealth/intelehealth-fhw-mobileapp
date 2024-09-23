package org.intelehealth.coreroomdb.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "tbl_encounter")
data class Encounter(
    @PrimaryKey
    @SerializedName("uuid")
    private var uuid: String? = null,
    @SerializedName("visituuid")
    var visitUuid: String? = null,
    @SerializedName("encounter_type_uuid")
    var encounterTypeUuid: String? = null,
    @SerializedName("encounter_time")
    var encounterTime: String? = null,
    @SerializedName("provider_uuid")
    var providerUuid: String? = null,
    @SerializedName("modified_date")
    var modifiedDate: String? = null,
    @SerializedName("sync")
    var sync: Boolean? = null,
    @SerializedName("voided")
    var voided: Int = 0,
    @SerializedName("privacynotice_value")
    var privacyNoticeValue: String? = null
) : Parcelable
