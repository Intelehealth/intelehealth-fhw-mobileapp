package org.intelehealth.coreroomdb.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "tbl_patient_attribute")
data class PatientAttribute(
    @PrimaryKey
    @SerializedName("uuid") var uuid: String,
    @SerializedName("value") var value: String? = null,
    @ColumnInfo("person_attribute_type_uuid") @SerializedName("person_attribute_type_uuid")
    var personAttributeTypeUuid: String? = null,
    @ColumnInfo("patientuuid") @SerializedName("patientuuid") var patientUuid: String? = null,
    @ColumnInfo("modified_date") @SerializedName("modified_date") var modifiedDate: String? = null,
    @SerializedName("voided") var voided: Int = 0,
    @SerializedName("sync") var sync: Boolean = false
) : Parcelable