package org.intelehealth.ncd.category.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import org.intelehealth.ncd.constants.Constants
import org.intelehealth.ncd.data.category.CategoryRepository
import org.intelehealth.ncd.model.Patient
import org.intelehealth.ncd.model.PatientAttributes
import org.intelehealth.ncd.model.PatientVisitDetails
import org.intelehealth.ncd.utils.CategorySegregationUtils
import java.util.UUID

class GeneralViewModel(private val repository: CategoryRepository, private val utils: CategorySegregationUtils) : ViewModel() {

  /*  private val _generalMutableLiveData = MutableLiveData<List<Patient>>()
    val generalLiveData: LiveData<List<Patient>> = _generalMutableLiveData*/
    private val _generalMutableLiveData = MutableLiveData<List<PatientVisitDetails>>()
    val generalLiveData: LiveData<List<PatientVisitDetails>> = _generalMutableLiveData
    private val allPatients = mutableListOf<PatientVisitDetails>()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    fun getPatientsForGeneral() {
        viewModelScope.launch(Dispatchers.IO) {
            val result: List<PatientVisitDetails> = repository.getPatientVisitDetailsBelowAgeForGeneral(Constants.ENCOUNTER_VISIT_COMPLETE)

            allPatients.clear()
            allPatients.addAll(result)

            _generalMutableLiveData.postValue(result)
        }
    }
    /*fun searchPatient(query: String) {
        val filtered = if (query.isBlank()) {
            allPatients
        } else {
            allPatients.filter {
                val name = "${it.firstName} ${it.middleName.orEmpty()} ${it.lastName.orEmpty()}".trim()
                val openmrsId = it.openmrsId ?: ""
                name.contains(query, ignoreCase = true) || openmrsId.contains(query, ignoreCase = true)
            }
        }
        _generalMutableLiveData.postValue(filtered)
    }*/
   /* fun getPatientFlow(encounterUuid: String): Flow<PagingData<PatientVisitDetails>> {
        return repository
            .getPagedPatients(encounterUuid)
            .cachedIn(viewModelScope)
    }*/
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun getPatientFlow(encounterUuid: String): Flow<PagingData<PatientVisitDetails>> {
        return searchQuery
            .debounce(300) // wait for user to finish typing
            .distinctUntilChanged()
            .flatMapLatest { query ->
                repository.getPagedPatients(encounterUuid, query)
            }
            .cachedIn(viewModelScope)
    }
}