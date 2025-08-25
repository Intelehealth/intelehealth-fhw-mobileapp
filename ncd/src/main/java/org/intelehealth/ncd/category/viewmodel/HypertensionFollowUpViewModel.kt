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
import org.intelehealth.ncd.utils.CategorySegregationUtils

class HypertensionFollowUpViewModel(
    private val repository: CategoryRepository,
    private val utils: CategorySegregationUtils
) : ViewModel() {

    private val _hypertensionFollowUpMutableLiveData = MutableLiveData<List<Patient>>()
    val hypertensionFollowUpLiveData: LiveData<List<Patient>> =
        _hypertensionFollowUpMutableLiveData

    fun getPatientsForHypertensionFollowUp(age: Int) {
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
    }
}