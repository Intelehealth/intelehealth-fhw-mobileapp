package org.intelehealth.coreroomdb.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "tbl_obs")
data class Observation(
    @PrimaryKey
    @SerializedName("uuid")
    private var uuid: String? = null,
    @SerializedName("encounteruuid")
    var encounterUuid: String? = null,
    @SerializedName("conceptuuid")
    var conceptUuid: String? = null,
    @SerializedName("value")
    var value: String? = null,
    @SerializedName("obsservermodifieddate")
    var obsServerModifiedDate: String? = null,
    @SerializedName("creator")
    var creator: String? = null,
    @SerializedName("comment")
    var comment: String? = null,
    @SerializedName("voided")
    var voided: Int? = null,
    @SerializedName("modified_date")
    var modifiedDate: String? = null,
    @SerializedName("created_date")
    var createdDate: String? = null,
    @SerializedName("sync")
    var sync: Boolean = false,
) : Parcelable