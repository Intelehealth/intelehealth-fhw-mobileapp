package org.intelehealth.ncd.category.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.intelehealth.ncd.constants.Constants
import org.intelehealth.ncd.data.SearchRepository
import org.intelehealth.ncd.model.Patient
import org.intelehealth.ncd.utils.CategorySegregationUtils

class AnemiaScreeningViewModel(
    private val repository: SearchRepository,
    private val utils: CategorySegregationUtils
) : ViewModel() {

    private val _anemiaScreeningMutableLiveData: MutableLiveData<List<Patient>> = MutableLiveData()
    val anemiaScreeningLiveData = _anemiaScreeningMutableLiveData

    fun getPatientsForAnemiaScreening(age: Int) {
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
    }
}