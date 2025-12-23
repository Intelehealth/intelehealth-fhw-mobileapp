package org.intelehealth.ncd.linelisting.utils

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.intelehealth.ncd.linelisting.viewmodels.ProtocolScreenViewModel


object SinglePatientHelper {

    // Kotlin helper



    interface Callback {
        fun onResult(eligibleMms: Map<String, Any?>)
    }
    @JvmStatic
    fun getSinglePatientEligibleMMS(
        patientUuid: String,
        viewModel: ProtocolScreenViewModel,
        callback: Callback
    ) {
        // Launch coroutine in ViewModel scope (or any CoroutineScope)
        viewModel.viewModelScope.launch(Dispatchers.IO) {
            val baseVisit = viewModel.repository.getPatientAndLatestVisitDetailsByPatientId(
                patientUuid = patientUuid,
                searchQuery = ""
            )

            if (baseVisit == null) {
                withContext(Dispatchers.Main) {
                    callback.onResult(emptyMap())
                }
                return@launch
            }

            // Enrich visit with protocol flags
            val allVisits = viewModel.repository.getAllVisitsForPatient(baseVisit.patientId)
            val flagsOnlyPatient = ProtocolParserHelper.parsePatientHistory(allVisits)

            val enrichedVisit = flagsOnlyPatient.copy(
                patientId = baseVisit.patientId,
                patientPhoto = baseVisit.patientPhoto,
                age = baseVisit.age,
                firstName = baseVisit.firstName,
                middleName = baseVisit.middleName,
                lastName = baseVisit.lastName,
                openmrsId = baseVisit.openmrsId,
                gender = baseVisit.gender,
                dateOfBirth = baseVisit.dateOfBirth,
                patientPhoneNumber = baseVisit.patientPhoneNumber,
                personAttributeTypeUuid = baseVisit.personAttributeTypeUuid,

                visitId = baseVisit.visitId,
                startDate = baseVisit.startDate,
                visitEndDate = baseVisit.visitEndDate,
                isPrescriptionExist = baseVisit.isPrescriptionExist,
                visitSpeciality = baseVisit.visitSpeciality,
                isNcdVisit = flagsOnlyPatient.isNcdVisit ?: baseVisit.isNcdVisit,

                value = flagsOnlyPatient.value ?: baseVisit.value,
                chiefComplaintData = flagsOnlyPatient.chiefComplaintData
                    ?: baseVisit.chiefComplaintData,
                isHypertensionFollowupGiven = flagsOnlyPatient.isHypertensionFollowupGiven,
                isHypertensionFollowupTodayOrLater = flagsOnlyPatient.isHypertensionFollowupTodayOrLater
            )
            // Step 3: get eligible MMS
            val eligibleMmsList = viewModel.categorySegregationUtils
                .getEligibleMMsForPatients(listOf(enrichedVisit))["eligible_mms"] ?: emptyList<String>()

            // Return map in desired format
            val resultMap = mapOf(
                "patient_id" to enrichedVisit.patientId,
                "eligible_mms" to eligibleMmsList
            )

            withContext(Dispatchers.Main) {
                callback.onResult(resultMap)
            }
        }
    }
}
