package org.intelehealth.ncd.linelisting.datasource

import androidx.paging.PagingSource
import org.intelehealth.ncd.constants.Constants
import org.intelehealth.ncd.model.PatientVisitDetails

class PatientVisitRepository(
    private val dataSource: PatientVisitDataSource
) {
    fun getPagedVisits(
        searchQuery: String?
    ): PagingSource<Int, PatientVisitDetails> {
        return dataSource.getPagedVisits(
            Constants.OTHER_MEDICAL_HISTORY,
            Constants.ENCOUNTER_VISIT_COMPLETE,
            searchQuery,
            Constants.IS_NCD_VISIT_ATTRIBUTE,
            Constants.PATIENT_PHONE,
            Constants.SPECIALITY,
            Constants.ENCOUNTER_ADULTINITIAL,
            Constants.CURRENT_COMPLAINT
        )
    }

    suspend fun getAllVisitsForPatient(
        patientUuid: String?,
    ): List<PatientVisitDetails> {
        return dataSource.getAllVisitsForPatient(patientUuid, Constants.IS_NCD_VISIT_ATTRIBUTE,
            Constants.ENCOUNTER_ADULTINITIAL, Constants.CURRENT_COMPLAINT)
    }

    fun getPatientAndLatestVisitDetailsByPatientId(
        patientUuid: String?,
        searchQuery: String?
    ): PatientVisitDetails {
        return dataSource.getPatientAndLatestVisitDetailsByPatientId(Constants.OTHER_MEDICAL_HISTORY, Constants.ENCOUNTER_VISIT_COMPLETE, patientUuid, searchQuery,
            Constants.IS_NCD_VISIT_ATTRIBUTE, Constants.PATIENT_PHONE, Constants.SPECIALITY,
            Constants.ENCOUNTER_ADULTINITIAL, Constants.CURRENT_COMPLAINT)
    }

    suspend fun getAllVisitsForPatientNew(
        patientUuid: List<String>,
    ): List<PatientVisitDetails> {
        return dataSource.getAllVisitsForPatientNew(patientUuid, Constants.OTHER_MEDICAL_HISTORY, Constants.ENCOUNTER_VISIT_COMPLETE, "searchQuery",
            Constants.IS_NCD_VISIT_ATTRIBUTE, Constants.PATIENT_PHONE, Constants.SPECIALITY,
            Constants.ENCOUNTER_ADULTINITIAL, Constants.CURRENT_COMPLAINT)
    }
}
