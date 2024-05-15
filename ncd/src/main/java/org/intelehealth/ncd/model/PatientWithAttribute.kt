package org.intelehealth.ncd.model

import androidx.room.ColumnInfo
import androidx.room.Ignore
import androidx.room.PrimaryKey

data class PatientWithAttribute(
    @PrimaryKey @ColumnInfo(name = "uuid") var uuid: String,
    @ColumnInfo(name = "openmrs_id") var openmrsId: String? = null,
    @ColumnInfo(name = "first_name") var firstName: String? = null,
    @ColumnInfo(name = "middle_name") var middleName: String? = null,
    @ColumnInfo(name = "last_name") var lastname: String? = null,
    @ColumnInfo(name = "date_of_birth") var dateOfBirth: String? = null,
    @ColumnInfo(name = "phone_number") var phoneNumber: String? = null,
    @ColumnInfo(name = "value") var value: String? = null,
    @ColumnInfo(name = "person_attribute_type_uuid") var personAttributeTypeUuid: String? = null,
) {
    @Ignore
    var attributeList: MutableList<String>? = null
}