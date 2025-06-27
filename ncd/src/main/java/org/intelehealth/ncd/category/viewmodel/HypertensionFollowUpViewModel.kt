package org.intelehealth.ncd.category.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    fun getPatientsForHypertensionFollowUp() {
        viewModelScope.launch {
            val result = repository.getPatientVisitDetails(
                age = Constants.HYPERTENSION_EXCLUSION_AGE,
                attributeTypeUuid = Constants.OTHER_MEDICAL_HISTORY,
                visitNoteEncounterUuid = Constants.VISIT_NOTE,
            )

            // Extract patient and attribute info
            val patientList = result.map {
                Patient(
                    uuid = it.patientId ?: "",
                    firstName = it.firstName,
                    middleName = it.middleName,
                    lastname = it.lastName,
                    gender = it.gender,
                    dateOfBirth = it.dateOfBirth
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

            // Now, filter original result using the filtered patient UUIDs
            val filteredResult = result.filter { detail ->
                filteredPatients.any { it.uuid == detail.patientId }
            }

            // Post filtered result
            _hypertensionFollowUpMutableLiveData.postValue(filteredResult)
        }
    }
}