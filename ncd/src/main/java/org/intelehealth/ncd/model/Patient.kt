package org.intelehealth.ncd.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tbl_patient")
data class Patient(
    @PrimaryKey var uuid: String,
    var openmrsId: String? = null,
    var firstname: String? = null,
    var middlename: String? = null,
    var lastname: String? = null,
    var dateofbirth: String? = null,
    var phonenumber: String? = null,
    var address2: String? = null,
    var address1: String? = null,
    var cityvillage: String? = null,
    var stateprovince: String? = null,
    var postalcode: String? = null,
    var country: String? = null,
    var education: String? = null,
    var economic: String? = null,
    var gender: String? = null,
    var patientPhoto: String? = null,
    var dead: Int? = null,
    var syncd: Boolean? = null
)
