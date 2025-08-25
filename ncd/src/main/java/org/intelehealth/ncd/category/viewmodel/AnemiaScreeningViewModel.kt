package org.intelehealth.ncd.category.viewmodel

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

class AnemiaScreeningViewModel(
    private val repository: CategoryRepository,
    private val utils: CategorySegregationUtils
) : ViewModel() {

  /*  private val _anemiaScreeningMutableLiveData: MutableLiveData<List<Patient>> = MutableLiveData()
    val anemiaScreeningLiveData = _anemiaScreeningMutableLiveData*/
    private val _anemiaScreeningMutableLiveData: MutableLiveData<List<PatientVisitDetails>> = MutableLiveData()
    val anemiaScreeningLiveData = _anemiaScreeningMutableLiveData
    private val allPatients = mutableListOf<PatientVisitDetails>()

    /*fun getPatientsForAnemiaScreening(age: Int) {
        var anemiaScreeningPatients: MutableList<Patient>

        viewModelScope.launch(Dispatchers.IO) {
            val patientsBasedOnAge = repository.getPatientsBasedOnAge(age)
            val patientsBasedOnUuids = repository.getPatientsBasedOnUuids(
                patientsBasedOnAge,
                Constants.OTHER_MEDICAL_HISTORY
            )

            anemiaScreeningPatients = utils.segregateAndFetchData(
                patientsBasedOnAge.toMutableList(),
                patientsBasedOnUuids.toMutableList(),
                Constants.ANEMIA_SCREENING
            )

            _anemiaScreeningMutableLiveData.postValue(anemiaScreeningPatients)
        }
    }*/

    fun getPatientsForAnemiaScreening() {
        viewModelScope.launch {
            val result = repository.getPatientVisitDetails(
                age = Constants.ANEMIA_EXCLUSION_AGE,
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
                category = Constants.ANEMIA_SCREENING
            )

            val filteredResult = result.filter { detail ->
                filteredPatients.any { it.uuid == detail.patientId }
            }

            // Save the full filtered result
            allPatients.clear()
            allPatients.addAll(filteredResult)

            // Post filtered result
            _anemiaScreeningMutableLiveData.postValue(filteredResult)
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
        _anemiaScreeningMutableLiveData.postValue(filtered)
    }

}