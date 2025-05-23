package org.intelehealth.ncd.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tbl_patient_attribute")
data class PatientAttributes(
    @PrimaryKey @ColumnInfo(name = "uuid") var uuid: String,
    @ColumnInfo(name = "value") var value: String? = null,
    @ColumnInfo(name = "person_attribute_type_uuid") var personAttributeTypeUuid: String? = null,
    @ColumnInfo(name = "patientuuid") var patientUuid: String? = null,
    @ColumnInfo(name = "modified_date") var modifiedDate: String? = null,
    @ColumnInfo(name = "voided") var voided: String? = null,
    @ColumnInfo(name = "sync") var sync: String? = null
)