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

    private val _anemiaScreeningMutableLiveData: MutableLiveData<List<PatientVisitDetails>> = MutableLiveData()
    val anemiaScreeningLiveData = _anemiaScreeningMutableLiveData
    private val allPatients = mutableListOf<PatientVisitDetails>()

    fun searchPatient(query: String) {
        val filtered = if (query.isBlank()) {
            allPatients
        } else {
            allPatients.filter {
                val name = "${it.firstName} ${it.middleName.orEmpty()} ${it.lastName.orEmpty()}".trim()
                val openmrsId = it.openmrsId ?: ""
                val phone = it.patientPhoneNumber ?: ""
                name.contains(query, ignoreCase = true) || openmrsId.contains(query, ignoreCase = true) || phone.contains(query, ignoreCase = true)
            }
        }
        _anemiaScreeningMutableLiveData.postValue(filtered)
    }
    fun getPatientsForAnemiaScreening() {
        viewModelScope.launch {
            val result = repository.getPatientVisitDetailsForFollowup(
                age = Constants.ANEMIA_EXCLUSION_AGE,
                attributeTypeUuid = Constants.OTHER_MEDICAL_HISTORY,
                visitNoteEncounterUuid = Constants.ENCOUNTER_VISIT_COMPLETE,
                ""
            )

            val filteredResult = utils.segregateAndFetchPatientVisitDetails(
                patientVisitDetailsList = result,
                category = Constants.ANEMIA_SCREENING
            )

            allPatients.clear()
            allPatients.addAll(filteredResult)

            _anemiaScreeningMutableLiveData.postValue(filteredResult)
        }
    }

}