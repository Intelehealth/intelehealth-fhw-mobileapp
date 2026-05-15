package org.intelehealth.ncd.category.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import org.intelehealth.ncd.data.category.CategoryRepository
import org.intelehealth.ncd.model.PatientVisitDetails
import org.intelehealth.ncd.room.dao.GeneralTabDao
import org.intelehealth.ncd.utils.CategorySegregationUtils

class GeneralViewModel(
    private val repository: CategoryRepository,
    @Suppress("unused") private val utils: CategorySegregationUtils,
) : ViewModel() {

    private val _generalMutableLiveData = MutableLiveData<List<PatientVisitDetails>>()
    val generalLiveData: LiveData<List<PatientVisitDetails>> = _generalMutableLiveData

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun getPatientFlow(generalTabDao: GeneralTabDao): Flow<PagingData<PatientVisitDetails>> {
        return searchQuery
            .debounce(300)
            .distinctUntilChanged()
            .flatMapLatest { query ->
                repository.getPagedPatients(query, generalTabDao)
            }
            .cachedIn(viewModelScope)
    }
}
