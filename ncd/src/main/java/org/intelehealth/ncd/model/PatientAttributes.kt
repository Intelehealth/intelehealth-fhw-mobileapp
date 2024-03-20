package org.intelehealth.ncd.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tbl_patient_attribute")
data class PatientAttributes(
    @PrimaryKey var uuid: String,
    var value: String? = null,
    var personAttributeTypeUuid: String? = null,
    var patientuuid: String? = null
)