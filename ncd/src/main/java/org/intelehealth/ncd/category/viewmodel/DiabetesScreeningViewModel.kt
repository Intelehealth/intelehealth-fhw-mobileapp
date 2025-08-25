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

class DiabetesScreeningViewModel(
    private val repository: CategoryRepository,
    private val utils: CategorySegregationUtils
) : ViewModel() {

    /*private val _diabetesScreeningMutableLiveData = MutableLiveData<List<Patient>>()
    val diabetesScreeningLiveData: LiveData<List<Patient>> = _diabetesScreeningMutableLiveData*/
    private val _diabetesScreeningMutableLiveData = MutableLiveData<List<PatientVisitDetails>>()
    val diabetesScreeningLiveData: LiveData<List<PatientVisitDetails>> = _diabetesScreeningMutableLiveData

    private val allPatients = mutableListOf<PatientVisitDetails>()

   /* fun getPatientsForDiabetesScreening(age: Int) {
        var diabetesScreeningPatients: MutableList<Patient>

        viewModelScope.launch(Dispatchers.IO) {
            val patientsBasedOnAge = repository.getPatientsBasedOnAge(age)
            val patientsBasedOnUuids = repository.getPatientsBasedOnUuids(
                patientsBasedOnAge,
                Constants.OTHER_MEDICAL_HISTORY
            )

            diabetesScreeningPatients = utils.segregateAndFetchData(
                patientsBasedOnAge.toMutableList(),
                patientsBasedOnUuids.toMutableList(),
                Constants.DIABETES_SCREENING
            )

            _diabetesScreeningMutableLiveData.postValue(diabetesScreeningPatients)
        }
    }*/
   fun getPatientsForDiabetesScreening() {
       viewModelScope.launch {
           val result = repository.getPatientVisitDetails(
               age = Constants.DIABETES_EXCLUSION_AGE,
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
               category = Constants.DIABETES_SCREENING
           )

           val filteredResult = result.filter { detail ->
               filteredPatients.any { it.uuid == detail.patientId }
           }

           // Save the full filtered result
           allPatients.clear()
           allPatients.addAll(filteredResult)

           // Post filtered result
           _diabetesScreeningMutableLiveData.postValue(filteredResult)
       }
   }
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
        _diabetesScreeningMutableLiveData.postValue(filtered)
    }
}