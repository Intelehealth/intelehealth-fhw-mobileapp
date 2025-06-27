package org.intelehealth.ncd.category.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.intelehealth.ncd.constants.Constants
import org.intelehealth.ncd.data.category.CategoryRepository
import org.intelehealth.ncd.model.Patient
import org.intelehealth.ncd.model.PatientAttributes
import org.intelehealth.ncd.model.PatientVisitDetails
import org.intelehealth.ncd.room.dao.VisitDao
import org.intelehealth.ncd.utils.CategorySegregationUtils
import org.intelehealth.ncd.utils.DateAndTimeUtils
import java.util.UUID

class HypertensionScreeningViewModel(
    private val repository: CategoryRepository,
    private val utils: CategorySegregationUtils
) : ViewModel() {

    private val _hypertensionScreeningMutableLiveData = MutableLiveData<List<PatientVisitDetails>>()
    val hypertensionScreeningLiveData: LiveData<List<PatientVisitDetails>> =
        _hypertensionScreeningMutableLiveData

    /*fun getPatientsForHypertensionScreening(age: Int) {
        var hypertensionScreeningPatients: MutableList<Patient>

        viewModelScope.launch(Dispatchers.IO) {
            val patientsBasedOnAge = repository.getPatientsBasedOnAge(age)
            val patientsBasedOnUuids = repository.getPatientsBasedOnUuids(
                patientsBasedOnAge,
                Constants.OTHER_MEDICAL_HISTORY
            )

            hypertensionScreeningPatients = utils.segregateAndFetchData(
                patientsBasedOnAge.toMutableList(),
                patientsBasedOnUuids.toMutableList(),
                Constants.HYPERTENSION_SCREENING
            )

            _hypertensionScreeningMutableLiveData.postValue(fetchDataForTags(hypertensionScreeningPatients))
        }
    }*/
   /* fun getPatientsForHypertensionScreening() {
        viewModelScope.launch {
            val result = repository.getPatientVisitDetails(
                age = Constants.HYPERTENSION_EXCLUSION_AGE,
                attributeTypeUuid = Constants.OTHER_MEDICAL_HISTORY,
                visitNoteEncounterUuid = Constants.VISIT_NOTE,
            )
            _hypertensionScreeningMutableLiveData.postValue(result)
        }
    }*/
    fun getPatientsForHypertensionScreening() {
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
                category = Constants.HYPERTENSION_SCREENING
            )

            // Now, filter original result using the filtered patient UUIDs
            val filteredResult = result.filter { detail ->
                filteredPatients.any { it.uuid == detail.patientId }
            }

            // Post filtered result
            _hypertensionScreeningMutableLiveData.postValue(filteredResult)
        }
    }


}