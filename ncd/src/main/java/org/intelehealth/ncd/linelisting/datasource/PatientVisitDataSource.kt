package org.intelehealth.ncd.linelisting.datasource

import androidx.paging.PagingSource
import org.intelehealth.ncd.linelisting.dao.PatientVisitDao
import org.intelehealth.ncd.model.PatientVisitDetails

class PatientVisitDataSource(
    private val dao: PatientVisitDao
) {

    fun getPagedVisits(
        attributeTypeUuid: String,
        visitNoteEncounterUuid: String,
        searchQuery: String?,
        ncdVisitAttribute: String,
        patientPhoneNoAttribute: String,
        visitSpecialityAttribute: String,
        chiefComplaintEncounterConceptUuid: String,
        chiefComplaintObsConceptUuid: String
    ): PagingSource<Int, PatientVisitDetails> {
        val safeSearchQuery = searchQuery ?: ""
        return dao.getAllVisitsPaged(attributeTypeUuid, visitNoteEncounterUuid,
            safeSearchQuery,ncdVisitAttribute, patientPhoneNoAttribute, visitSpecialityAttribute,
            chiefComplaintEncounterConceptUuid, chiefComplaintObsConceptUuid)
    }

    suspend fun getAllVisitsForPatient(
        patientUuid: String?,
        ncdVisitAttribute: String,
        chiefComplaintEncounterConceptUuid: String,
        chiefComplaintObsConceptUuid: String
    ): List<PatientVisitDetails> {
        val safePatientUuid = patientUuid ?: ""
        return dao.getNcdCompletedVisitsForProtocolFlags(safePatientUuid,ncdVisitAttribute,
            chiefComplaintEncounterConceptUuid, chiefComplaintObsConceptUuid)
    }

    fun getPatientAndLatestVisitDetailsByPatientId(
        attributeTypeUuid: String,
        visitNoteEncounterUuid: String,
        patientUuid: String?,
        searchQuery: String?,
        ncdVisitAttribute: String,
        patientPhoneNoAttribute: String,
        visitSpecialityAttribute: String,
        chiefComplaintEncounterConceptUuid: String,
        chiefComplaintObsConceptUuid: String
    ):PatientVisitDetails {
        val safePatientUuid = patientUuid ?: ""
        val safeSearchQuery = searchQuery ?: ""
        return dao.getPatientAndLatestVisitDetailsByPatientId(attributeTypeUuid, visitNoteEncounterUuid, safePatientUuid,
            safeSearchQuery,ncdVisitAttribute, patientPhoneNoAttribute, visitSpecialityAttribute, chiefComplaintEncounterConceptUuid, chiefComplaintObsConceptUuid)
    }
}
