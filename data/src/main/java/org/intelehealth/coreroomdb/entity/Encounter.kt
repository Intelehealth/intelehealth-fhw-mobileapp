package org.intelehealth.coreroomdb.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "tbl_encounter")
data class Encounter(
    @PrimaryKey @SerializedName("uuid") var uuid: String,
    @ColumnInfo("visituuid") @SerializedName("visituuid") var visitUuid: String? = null,
    @ColumnInfo("encounter_type_uuid") @SerializedName("encounter_type_uuid") var encounterTypeUuid: String? = null,
    @ColumnInfo("encounter_time") @SerializedName("encounter_time") var encounterTime: String? = null,
    @ColumnInfo("provider_uuid") @SerializedName("provider_uuid") var providerUuid: String? = null,
    @ColumnInfo("modified_date") @SerializedName("modified_date") var modifiedDate: String? = null,
    @SerializedName("sync") var sync: Boolean? = null,
    @SerializedName("voided") var voided: Int = 0,
    @ColumnInfo("privacynotice_value") @SerializedName("privacynotice_value") var privacyNoticeValue: String? = null
) : Parcelable
