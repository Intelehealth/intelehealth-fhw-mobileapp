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

    @ColumnInfo(name = "openmrs_id")
    var openmrsId: String? = null,

    @ColumnInfo(name = "attributeValue")
    var value: String? = null,

    @ColumnInfo(name = "attributeTypeUuid")
    var personAttributeTypeUuid: String? = null,

    @ColumnInfo(name = "visitStartDate")
    var startDate: String? = null,

    @ColumnInfo(name = "prescriptionExists")
    var isPrescriptionExist: Boolean? = null,

    @ColumnInfo(name = "modified_date")
    var modifiedDate: String? = null,

    @ColumnInfo(name = "is_ncd_visit")
    var isNcdVisit: String? = null,

    @ColumnInfo(name = "chief_complaint_data")
    var chiefComplaintData: String? = null,

    @ColumnInfo(name = "follow_up_from_protocol")
    var followUpFromProtocol: Boolean? = null,

    @ColumnInfo(name = "visit_speciality")
    var visitSpeciality: String? = null,

    @ColumnInfo(name = "visitEndDate")
    var visitEndDate: String? = null,

    @ColumnInfo(name = "isFollowUpDateGivenToPatient")
    var isFollowUpDateGivenToPatient: Boolean? = null

) {
    @Ignore
    var attributeList: MutableList<String>? = mutableListOf()
}
