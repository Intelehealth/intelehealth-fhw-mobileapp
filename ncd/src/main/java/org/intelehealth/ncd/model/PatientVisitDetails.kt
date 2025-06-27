package org.intelehealth.ncd.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import kotlinx.parcelize.Parcelize


data class PatientVisitDetails(
    @ColumnInfo(name = "patientId")
    var patientId: String? = null,

    @ColumnInfo(name = "patientPhoto")
    var patientPhoto: String? = null,

    @ColumnInfo(name = "visitId")
    var visitId: String? = null,

    @ColumnInfo(name = "firstName")
    var firstName: String? = null,

    @ColumnInfo(name = "middleName")
    var middleName: String? = null,

    @ColumnInfo(name = "lastName")
    var lastName: String? = null,

    @ColumnInfo(name = "dateOfBirth")
    var dateOfBirth: String? = null,

    @ColumnInfo(name = "gender")
    var gender: String? = null,

    @ColumnInfo(name = "attributeValue")
    var value: String? = null,

    @ColumnInfo(name = "attributeTypeUuid")
    var personAttributeTypeUuid: String? = null,

    @ColumnInfo(name = "visitStartDate")
    var startDate: String? = null,

    @ColumnInfo(name = "prescriptionExists")
    var isPrescriptionExist: Boolean? = null,

    @ColumnInfo(name = "modified_date")
    var modifiedDate: String? = null
) {
    @Ignore
    var attributeList: MutableList<String>? = mutableListOf()
}
