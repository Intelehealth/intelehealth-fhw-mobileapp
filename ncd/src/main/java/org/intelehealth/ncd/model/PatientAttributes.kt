package org.intelehealth.ncd.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


/*

@Entity(tableName = "tbl_patient_attribute")
data class PatientAttributes(
    @PrimaryKey @ColumnInfo(name = "uuid") var uuid: String,
    @ColumnInfo(name = "value") var value: String? = null,
    @ColumnInfo(name = "person_attribute_type_uuid") var personAttributeTypeUuid: String? = null,
    @ColumnInfo(name = "patientuuid") var patientUuid: String? = null,
    @ColumnInfo(name = "modified_date") var modifiedDate: String? = null,
    @ColumnInfo(name = "voided") var voided: String? = null,
    @ColumnInfo(name = "sync") var sync: String? = null
)*/

@Entity(tableName = "tbl_patient_attribute")
data class PatientAttributes(
    @PrimaryKey
    @ColumnInfo(name = "uuid")
    val uuid: String,
    @ColumnInfo(name = "value")
    val value: String? = null,
    @ColumnInfo(name = "person_attribute_type_uuid")
    val personAttributeTypeUuid: String? = null,
    @ColumnInfo(name = "patientuuid")
    val patientUuid: String? = null,
    @ColumnInfo(name = "modified_date")
    val modifiedDate: String? = null,
    @ColumnInfo(name = "voided", defaultValue = "'0'")
    val voided: String? = "0",
    @ColumnInfo(name = "sync", defaultValue = "'false'")
    val sync: String? = "false"
)






