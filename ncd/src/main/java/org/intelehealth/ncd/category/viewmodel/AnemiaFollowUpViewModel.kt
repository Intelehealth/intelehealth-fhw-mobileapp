package org.intelehealth.ncd.category.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.intelehealth.ncd.constants.Constants
import org.intelehealth.ncd.data.category.CategoryRepository
import org.intelehealth.ncd.model.Patient
import org.intelehealth.ncd.utils.CategorySegregationUtils

class AnemiaFollowUpViewModel(
    private val repository: CategoryRepository,
    private val utils: CategorySegregationUtils
) : ViewModel() {

    private val _anemiaFollowUpMutableLiveData: MutableLiveData<List<Patient>> = MutableLiveData()
    val anemiaFollowUpLiveData = _anemiaFollowUpMutableLiveData

    fun getPatientsForAnemiaFollowUp(age: Int) {
        var anemiaFollowUpPatients: MutableList<Patient>

        viewModelScope.launch(Dispatchers.IO) {
            val patientsBasedOnAge = repository.getPatientsBasedOnAge(age)
            val patientsBasedOnUuids = repository.getPatientsBasedOnUuids(
                patientsBasedOnAge,
                Constants.OTHER_MEDICAL_HISTORY
            )

            anemiaFollowUpPatients = utils.segregateAndFetchData(
                patientsBasedOnAge.toMutableList(),
                patientsBasedOnUuids.toMutableList(),
                Constants.ANEMIA_FOLLOW_UP
            )

            _anemiaFollowUpMutableLiveData.postValue(anemiaFollowUpPatients)
        }
    }

}