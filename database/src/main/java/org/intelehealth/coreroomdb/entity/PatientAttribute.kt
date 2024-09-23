package org.intelehealth.coreroomdb.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "tbl_patient_attribute")
data class PatientAttribute(
    @SerializedName("uuid")
    @PrimaryKey
    var uuid: String,
    @SerializedName("value")
    var value: String? = null,
    @SerializedName("person_attribute_type_uuid")
    var personAttributeTypeUuid: String? = null,
    @SerializedName("patientuuid")
    var patientUuid: String? = null,
    @SerializedName("modified_date")
    var modifiedDate: String? = null,
    @SerializedName("voided")
    var voided: Int = 0,
    @SerializedName("sync")
    var sync: Boolean = false
) : Parcelable