package org.intelehealth.ncd.linelisting.datasource

import androidx.paging.PagingSource
import org.intelehealth.ncd.linelisting.dao.PatientVisitDao
import org.intelehealth.ncd.model.PatientVisitDetails

class PatientVisitDataSource(
    private val dao: PatientVisitDao
) {

    fun getPagedVisits(
        age: Int,
        attributeTypeUuid: String,
        visitNoteEncounterUuid: String,
        patientUuid: String?,
        searchQuery: String?
    ): PagingSource<Int, PatientVisitDetails> {
        val safePatientUuid = patientUuid ?: ""
        val safeSearchQuery = searchQuery ?: ""
        return dao.getAllVisitsPaged(age, attributeTypeUuid, visitNoteEncounterUuid, safePatientUuid, safeSearchQuery)
    }

    suspend fun getAllVisitsForPatient(
        patientUuid: String?
    ): List<PatientVisitDetails> {
        val safePatientUuid = patientUuid ?: ""
        return dao.getNcdCompletedVisitsForProtocolFlags(safePatientUuid)
    }

    fun getPatientAndLatestVisitDetailsByPatientId(
        age: Int,
        attributeTypeUuid: String,
        visitNoteEncounterUuid: String,
        patientUuid: String?,
        searchQuery: String?
    ):PatientVisitDetails {
        val safePatientUuid = patientUuid ?: ""
        val safeSearchQuery = searchQuery ?: ""
        return dao.getPatientAndLatestVisitDetailsByPatientId(age, attributeTypeUuid, visitNoteEncounterUuid, safePatientUuid, safeSearchQuery)
    }
}
