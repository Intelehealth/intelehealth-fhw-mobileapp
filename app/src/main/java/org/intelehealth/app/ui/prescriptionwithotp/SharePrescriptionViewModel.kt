package org.intelehealth.app.ui.prescriptionwithotp

import android.database.sqlite.SQLiteDatabase
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class SharePrescriptionViewModel (private val db: SQLiteDatabase) : ViewModel() {
    private  val TAG = "SharePrescriptionViewMo"
    private val repository = SharePrescriptionDataRepository(db)

    private suspend fun loadPrescriptionData(patientUuid: String, visitUuid: String): PrescriptionData {
        val patient = repository.getPatientDetails(patientUuid)
        val encounterMap = repository.getEncountersUuidByVisitUuid(visitUuid) ?: emptyMap()
        val vitalEncounter = encounterMap[PrescriptionDetailsDataKeys.EncounterType.VITAL.key]
        val adultInitialEncounter = encounterMap[PrescriptionDetailsDataKeys.EncounterType.ADULT_INITIAL.key]
        val visitCompleteEncounter = encounterMap[PrescriptionDetailsDataKeys.EncounterType.VISIT_COMPLETE.key]

        val vitals = vitalEncounter?.let { repository.getVitals(it) }
        val diagnostics = vitalEncounter?.let { repository.getDiagnostics(it) }
        val adultInitial = adultInitialEncounter?.let { repository.getAdultInitialData(it) }
        val visitCompleteEncData = visitCompleteEncounter?.let { repository.getVisitCompleteEncounterData(it) }
        return PrescriptionData(patient = patient, vitals = vitals, diagnostics = diagnostics, adultInitials = adultInitial, visitCompleteEncData = visitCompleteEncData)
    }

    fun loadPrescriptionDataFromJava(
        patientUuid: String,
        visitID: String,
        onSuccess: (PrescriptionData) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            try { val data = loadPrescriptionData(patientUuid, visitID)
                onSuccess(data)
            } catch (e: Exception) {
                onError(e)
            }
        }
    }
}
