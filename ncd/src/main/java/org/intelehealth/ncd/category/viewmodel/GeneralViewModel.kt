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

class GeneralViewModel(private val repository: CategoryRepository) : ViewModel() {

    private val _generalMutableLiveData = MutableLiveData<List<Patient>>()
    val generalLiveData: LiveData<List<Patient>> = _generalMutableLiveData

    fun getPatientsForGeneral() {
        viewModelScope.launch(Dispatchers.IO) {
            val patientsList: List<Patient> = repository.getPatientsBelowAge(
                Constants.GENERAL_EXCLUSION_AGE
            )

            _generalMutableLiveData.postValue(patientsList)
        }
    }
}