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

class GeneralViewModel(private val repository: CategoryRepository, private val utils: CategorySegregationUtils) : ViewModel() {

  /*  private val _generalMutableLiveData = MutableLiveData<List<Patient>>()
    val generalLiveData: LiveData<List<Patient>> = _generalMutableLiveData*/
    private val _generalMutableLiveData = MutableLiveData<List<PatientVisitDetails>>()
    val generalLiveData: LiveData<List<PatientVisitDetails>> = _generalMutableLiveData
    private val allPatients = mutableListOf<PatientVisitDetails>()


    fun getPatientsForGeneral() {
        viewModelScope.launch(Dispatchers.IO) {
            val result: List<PatientVisitDetails> = repository.getPatientVisitDetailsBelowAgeForGeneral(
                Constants.GENERAL_EXCLUSION_AGE,
                Constants.ENCOUNTER_VISIT_COMPLETE
            )

          /*  // Extract patient and attribute info
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
            }*/

            // Save the full filtered result
            allPatients.clear()
            allPatients.addAll(result)

            // Post initial full list to LiveData
            _generalMutableLiveData.postValue(result)
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
        _generalMutableLiveData.postValue(filtered)
    }
}