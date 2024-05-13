package org.intelehealth.ncd.category.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.intelehealth.ncd.constants.Constants
import org.intelehealth.ncd.data.SearchRepository
import org.intelehealth.ncd.model.Patient
import org.intelehealth.ncd.utils.CategorySegregationUtils

class DiabetesScreeningViewModel(
    private val repository: SearchRepository,
    private val utils: CategorySegregationUtils
) : ViewModel() {

    private val _diabetesScreeningMutableLiveData = MutableLiveData<List<Patient>>()
    val diabetesScreeningLiveData: LiveData<List<Patient>> = _diabetesScreeningMutableLiveData

    fun getPatientsForDiabetesScreening(age: Int) {
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
    }
}