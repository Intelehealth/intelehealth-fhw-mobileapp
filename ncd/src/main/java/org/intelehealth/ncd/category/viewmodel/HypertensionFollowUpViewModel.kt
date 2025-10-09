package org.intelehealth.ncd.category.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.intelehealth.ncd.constants.Constants
import org.intelehealth.ncd.data.category.CategoryRepository
import org.intelehealth.ncd.model.Patient
import org.intelehealth.ncd.model.PatientAttributes
import org.intelehealth.ncd.model.PatientVisitDetails
import org.intelehealth.ncd.utils.CategorySegregationUtils
import java.util.UUID

class HypertensionFollowUpViewModel(
    private val repository: CategoryRepository,
    private val utils: CategorySegregationUtils
) : ViewModel() {

    /*private val _hypertensionFollowUpMutableLiveData = MutableLiveData<List<Patient>>()
    val hypertensionFollowUpLiveData: LiveData<List<Patient>> = _hypertensionFollowUpMutableLiveData*/
    private val _hypertensionFollowUpMutableLiveData = MutableLiveData<List<PatientVisitDetails>>()
    val hypertensionFollowUpLiveData: LiveData<List<PatientVisitDetails>> = _hypertensionFollowUpMutableLiveData
    private val allPatients = mutableListOf<PatientVisitDetails>()

   /* fun getPatientsForHypertensionFollowUp(age: Int) {
        var hypertensionFollowUpPatients: MutableList<Patient>

        viewModelScope.launch(Dispatchers.IO) {
            val patientsBasedOnAge = repository.getPatientsBasedOnAge(age)
            val patientsBasedOnUuids = repository.getPatientsBasedOnUuids(
                patientsBasedOnAge,
                Constants.OTHER_MEDICAL_HISTORY
            )

            hypertensionFollowUpPatients = utils.segregateAndFetchData(
                patientsBasedOnAge.toMutableList(),
                patientsBasedOnUuids.toMutableList(),
                Constants.HYPERTENSION_FOLLOW_UP
            )

            _hypertensionFollowUpMutableLiveData.postValue(hypertensionFollowUpPatients)
        }
    }*/

    /*fun getPatientsForHypertensionFollowUp() {
        viewModelScope.launch {
            val result = repository.getPatientVisitDetailsForFollowup(
                age = Constants.HYPERTENSION_EXCLUSION_AGE,
                attributeTypeUuid = Constants.OTHER_MEDICAL_HISTORY,
                visitNoteEncounterUuid = Constants.ENCOUNTER_VISIT_COMPLETE,
            )

            // Extract patient and attribute info
            val patientList = result.map {
                Patient(
                    uuid = it.patientId ?: "",
                    firstName = it.firstName,
                    middleName = it.middleName,
                    lastname = it.lastName,
                    gender = it.gender,
                    dateOfBirth = it.dateOfBirth,
                    openmrs_id = it.openmrsId
                )
            }.toMutableList()

            val attributeList = result.map {
                PatientAttributes(
                    uuid = UUID.randomUUID().toString(),
                    patientUuid = it.patientId ?: "",
                    value = it.value
                )
            }.toMutableList()

            // Filter using segregation utility
            val filteredPatients = utils.segregateAndFetchData(
                patientList = patientList,
                patientAttributeList = attributeList,
                category = Constants.HYPERTENSION_FOLLOW_UP
            )

            val filteredResult = result.filter { detail ->
                filteredPatients.any { it.uuid == detail.patientId }
            }

            // Save the full filtered result
            allPatients.clear()
            allPatients.addAll(filteredResult)

            // Post initial full list to LiveData
            Log.d("TAG", "getPatientsForHypertensionFollowUp: filteredResult : "+filteredResult)
            _hypertensionFollowUpMutableLiveData.postValue(filteredResult)
        }
    }*/
    fun searchPatient(query: String) {
        val filtered = if (query.isBlank()) {
            allPatients
        } else {
            allPatients.filter {
                val name = "${it.firstName} ${it.middleName.orEmpty()} ${it.lastName.orEmpty()}".trim()
                val openmrsId = it.openmrsId ?: ""
                name.contains(query, ignoreCase = true) || openmrsId.contains(query, ignoreCase = true)
            }
        }
        _hypertensionFollowUpMutableLiveData.postValue(filtered)
    }
    fun getPatientsForHypertensionFollowUp() {
        viewModelScope.launch {
            val result = repository.getPatientVisitDetailsForFollowup(
                age = Constants.HYPERTENSION_EXCLUSION_AGE,
                attributeTypeUuid = Constants.OTHER_MEDICAL_HISTORY,
                visitNoteEncounterUuid = Constants.ENCOUNTER_VISIT_COMPLETE,
            )
            Log.d("TAG", "getPatientsForHypertensionFollowUp: result size : "+result)
            Log.d("TAG", "getPatientsForHypertensionFollowUp: result data : "+ Gson().toJson(result))

            // Filter directly on PatientVisitDetails without mapping
            val filteredResult = utils.segregateAndFetchPatientVisitDetails(
                patientVisitDetailsList = result,
                category = Constants.HYPERTENSION_FOLLOW_UP
            )

            allPatients.clear()
            allPatients.addAll(filteredResult)

            _hypertensionFollowUpMutableLiveData.postValue(filteredResult)
        }
    }

}