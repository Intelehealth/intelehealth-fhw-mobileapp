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
import org.intelehealth.ncd.model.PatientVisitDetails

class GeneralViewModel(private val repository: CategoryRepository) : ViewModel() {

  /*  private val _generalMutableLiveData = MutableLiveData<List<Patient>>()
    val generalLiveData: LiveData<List<Patient>> = _generalMutableLiveData*/
    private val _generalMutableLiveData = MutableLiveData<List<PatientVisitDetails>>()
    val generalLiveData: LiveData<List<PatientVisitDetails>> = _generalMutableLiveData

    fun getPatientsForGeneral() {
        viewModelScope.launch(Dispatchers.IO) {
            val patientsList: List<PatientVisitDetails> = repository.getPatientVisitDetailsBelowAgeForGeneral(
                Constants.GENERAL_EXCLUSION_AGE,
                Constants.VISIT_NOTE
            )

            _generalMutableLiveData.postValue(patientsList)
        }
    }
}