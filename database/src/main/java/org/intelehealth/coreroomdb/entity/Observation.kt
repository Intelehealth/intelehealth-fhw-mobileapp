package org.intelehealth.coreroomdb.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "tbl_obs")
data class Observation(
    @PrimaryKey
    @SerializedName("uuid") var uuid: String,
    @ColumnInfo("encounteruuid") @SerializedName("encounteruuid") var encounterUuid: String? = null,
    @ColumnInfo("conceptuuid") @SerializedName("conceptuuid", alternate = ["concept"]) var conceptUuid: String? = null,
    @SerializedName("value") var value: String? = null,
    @ColumnInfo("obsservermodifieddate") @SerializedName("obsservermodifieddate") var obsServerModifiedDate: String? = null,
    @Ignore @SerializedName("obsDatetime") @Expose var obsDatetime: String,
    @Ignore @SerializedName("encounter") @Expose var encounter: String,
    @SerializedName("creator", alternate = ["person"]) var creator: String? = null,
    @SerializedName("comment") var comment: String? = null,
    @SerializedName("voided") var voided: Int? = null,
    @ColumnInfo("modified_date") @SerializedName("modified_date") var modifiedDate: String? = null,
    @ColumnInfo("created_date") @SerializedName("created_date") var createdDate: String? = null,
    @SerializedName("sync") var sync: Boolean = false,
) : Parcelable

data class ObsJsonResponse(

    @SerializedName("uuid") var uuid: String,
    @SerializedName("display") var display: String,
    @SerializedName("concept") var concept: Concept,
    @SerializedName("person") var person: Person,
    @SerializedName("obsDatetime") var obsDatetime: String,
    @SerializedName("accessionNumber") var accessionNumber: String,
    @SerializedName("obsGroup") var obsGroup: String,
    @SerializedName("valueCodedName") var valueCodedName: String,
    @SerializedName("groupMembers") var groupMembers: String,
    @SerializedName("comment") var comment: String,
    @SerializedName("location") var location: String,
    @SerializedName("order") var order: String,
    @SerializedName("encounter") var encounter: Encounter,
    @SerializedName("voided") var voided: Boolean,
    @SerializedName("value") var value: Person,
    @SerializedName("valueModifier") var valueModifier: String,
    @SerializedName("formFieldPath") var formFieldPath: String,
    @SerializedName("formFieldNamespace") var formFieldNamespace: String,
    @SerializedName("links") var links: List<Link>,
    @SerializedName("resourceVersion") var resourceVersion: String

)