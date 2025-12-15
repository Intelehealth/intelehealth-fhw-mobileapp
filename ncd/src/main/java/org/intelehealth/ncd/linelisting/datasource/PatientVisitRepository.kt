package org.intelehealth.ncd.linelisting.datasource

import androidx.paging.PagingSource
import org.intelehealth.ncd.model.PatientVisitDetails

class PatientVisitRepository(
    private val dataSource: PatientVisitDataSource
) {
    fun getPagedVisits(
        age: Int,
        attributeTypeUuid: String,
        visitNoteEncounterUuid: String,
        patientUuid: String?,
        searchQuery: String?
    ): PagingSource<Int, PatientVisitDetails> {
        return dataSource.getPagedVisits(attributeTypeUuid, visitNoteEncounterUuid, searchQuery)
    }

    suspend fun getAllVisitsForPatient(
        patientUuid: String?,
    ): List<PatientVisitDetails> {
        return dataSource.getAllVisitsForPatient(patientUuid)
    }

    fun getPatientAndLatestVisitDetailsByPatientId(
        age: Int,
        attributeTypeUuid: String,
        visitNoteEncounterUuid: String,
        patientUuid: String?,
        searchQuery: String?
    ): PatientVisitDetails {
        return dataSource.getPatientAndLatestVisitDetailsByPatientId(attributeTypeUuid, visitNoteEncounterUuid, patientUuid, searchQuery)
    }
}
