package org.intelehealth.ncd.search.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.intelehealth.ncd.constants.Constants
import org.intelehealth.ncd.data.SearchRepository
import org.intelehealth.ncd.model.Patient
import org.intelehealth.ncd.model.PatientAttributes
import org.intelehealth.ncd.utils.CategorySegregationUtils

class GeneralViewModel(
    private val repository: SearchRepository,
    private val utils: CategorySegregationUtils
) : ViewModel() {

    private val _generalMutableLiveData = MutableLiveData<List<Patient>>()
    val generalLiveData: LiveData<List<Patient>> = _generalMutableLiveData

    fun getPatientsForGeneral() {
        var generalPatients: MutableList<Patient>

        viewModelScope.launch(Dispatchers.IO) {
            val patientsList: List<Patient> = repository.getAllPatients()
            val patientAttributes: List<PatientAttributes> = repository.getPatientsBasedOnUuids(
                patientsList,
                Constants.OTHER_MEDICAL_HISTORY
            )

            generalPatients = utils.segregateAndFetchData(
                patientsList.toMutableList(),
                patientAttributes.toMutableList(),
                Constants.GENERAL
            )

            _generalMutableLiveData.postValue(generalPatients)
        }
    }
}