package org.intelehealth.ncd.data.category

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import org.intelehealth.ncd.constants.Constants
import org.intelehealth.ncd.model.Patient
import org.intelehealth.ncd.model.PatientAttributes
import org.intelehealth.ncd.model.PatientVisitDetails
import org.intelehealth.ncd.model.PrescriptionExistsResult
import org.intelehealth.ncd.model.Visit
import org.intelehealth.ncd.model.VisitAttributeResult
import org.intelehealth.ncd.room.dao.GeneralTabDao
import org.intelehealth.ncd.room.dao.PatientAttributeDao
import org.intelehealth.ncd.room.dao.PatientDao
import org.intelehealth.ncd.room.dao.VisitDao

class CategoryDataSource(
    private val patientDao: PatientDao,
    private val patientAttributeDao: PatientAttributeDao,
    private val visitDao: VisitDao,
    private val generalDao: GeneralTabDao? = null
) {
    suspend fun getPatientVisitRawData(age: Int,  attributeTypeUuid: String, visitNoteEncounterUuid: String): List<PatientVisitDetails> = visitDao.getPatientVisitRawData(age, attributeTypeUuid,visitNoteEncounterUuid)
    suspend fun getPatientVisitRawDataForFollowup(age: Int,  attributeTypeUuid: String, visitNoteEncounterUuid: String,patientUuid: String): List<PatientVisitDetails> = visitDao.getPatientVisitRawDataForFollowup(age, attributeTypeUuid,visitNoteEncounterUuid, patientUuid)

    suspend fun getPatientsAndVisitsPage(limit: Int, offset: Int, patientPhoneNoAttribute: String): List<PatientVisitDetails> {
        return generalDao?.getPatientsAndVisitsPage(limit, offset, patientPhoneNoAttribute) ?: emptyList()
    }

    suspend fun getSearchPatientsPage(
        query: String,
        limit: Int,
        offset: Int,
        encounterUuid: String,
        ncdAttrUuid: String,
        specialityAttrUuid: String,
        phoneAttrUuid: String
    ): List<PatientVisitDetails> {
        return generalDao?.getPagedPatientsSql(
            query,
            encounterUuid,
            ncdAttrUuid,
            specialityAttrUuid,
            phoneAttrUuid,
            limit,
            offset
        ) ?: emptyList()
    }

    suspend fun getVisitAttributesBatch(visitIds: List<String>): List<VisitAttributeResult> {
        return generalDao?.getVisitAttributesBatch(
            visitIds,
            listOf(Constants.IS_NCD_VISIT_ATTRIBUTE, Constants.SPECIALITY)
        ) ?: emptyList()
    }
    suspend fun getPrescriptionExistsBatch(encounterUuid: String, visitIds: List<String>): List<PrescriptionExistsResult> {
        return generalDao?.getPrescriptionExistsBatch(encounterUuid, visitIds) ?: emptyList()
    }

}
