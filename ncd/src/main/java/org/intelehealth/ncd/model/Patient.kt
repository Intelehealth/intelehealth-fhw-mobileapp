package org.intelehealth.ncd.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tbl_patient")
data class Patient(
    @PrimaryKey @ColumnInfo(name = "uuid") var uuid: String,
    @ColumnInfo(name = "openmrs_id") var openmrsId: String? = null,
    @ColumnInfo(name = "first_name") var firstName: String? = null,
    @ColumnInfo(name = "middle_name") var middleName: String? = null,
    @ColumnInfo(name = "last_name") var lastname: String? = null,
    @ColumnInfo(name = "date_of_birth") var dateOfBirth: String? = null,
    @ColumnInfo(name = "phone_number") var phoneNumber: String? = null,
    @ColumnInfo(name = "address1") var address1: String? = null,
    @ColumnInfo(name = "address2") var address2: String? = null,
    @ColumnInfo(name = "city_village") var cityVillage: String? = null,
    @ColumnInfo(name = "state_province") var stateProvince: String? = null,
    @ColumnInfo(name = "postal_code") var postalCode: String? = null,
    @ColumnInfo(name = "country") var country: String? = null,
    @ColumnInfo(name = "gender") var gender: String? = null,
    @ColumnInfo(name = "sdw") var sdw: String? = null,
    @ColumnInfo(name = "occupation") var occupation: String? = null,
    @ColumnInfo(name = "patient_photo") var patientPhoto: String? = null,
    @ColumnInfo(name = "economic_status") var economicStatus: String? = null,
    @ColumnInfo(name = "education_status") var educationStatus: String? = null,
    @ColumnInfo(name = "caste") var caste: String? = null,
    @ColumnInfo(name = "dead") var dead: String? = null,
    @ColumnInfo(name = "modified_date") var modifiedDate: String? = null,
    @ColumnInfo(name = "voided") var voided: String? = "0",
    @ColumnInfo(name = "sync") var syncd: String? = "false"
)
