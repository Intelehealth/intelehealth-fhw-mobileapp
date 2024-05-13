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

class HypertensionScreeningViewModel(
    private val repository: SearchRepository,
    private val utils: CategorySegregationUtils
) : ViewModel() {

    private val _hypertensionScreeningMutableLiveData = MutableLiveData<List<Patient>>()
    val hypertensionScreeningLiveData: LiveData<List<Patient>> =
        _hypertensionScreeningMutableLiveData

    fun getPatientsForHypertensionScreening(age: Int) {
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

            _hypertensionScreeningMutableLiveData.postValue(hypertensionScreeningPatients)
        }
    }
}