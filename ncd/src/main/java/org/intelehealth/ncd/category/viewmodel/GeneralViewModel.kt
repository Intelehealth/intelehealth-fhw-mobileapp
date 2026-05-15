package org.intelehealth.ncd.category.viewmodel

import android.os.SystemClock
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import org.intelehealth.ncd.data.category.CategoryRepository
import org.intelehealth.ncd.model.PatientVisitDetails
import org.intelehealth.ncd.room.dao.GeneralTabDao
import org.intelehealth.ncd.utils.CategorySegregationUtils

class GeneralViewModel(private val repository: CategoryRepository, private val utils: CategorySegregationUtils) : ViewModel() {

    companion object {
        private const val LOG_TAG = "Pooja"
    }

    private val _generalMutableLiveData = MutableLiveData<List<PatientVisitDetails>>()
    val generalLiveData: LiveData<List<PatientVisitDetails>> = _generalMutableLiveData

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    fun onSearchQueryChanged(query: String) {
        val t0 = SystemClock.elapsedRealtime()
        _searchQuery.value = query
        Log.d(
            LOG_TAG,
            "GeneralViewModel.onSearchQueryChanged len=${query.length} assignStateFlow +${SystemClock.elapsedRealtime() - t0}ms elapsedMs=${SystemClock.elapsedRealtime()}"
        )
    }

    fun getPatientFlow(generalTabDao: GeneralTabDao): Flow<PagingData<PatientVisitDetails>> {
        return searchQuery
            .onEach { q ->
                Log.d(
                    LOG_TAG,
                    "GeneralViewModel pipeline: searchQuery emit len=${q.length} thread=${Thread.currentThread().name} elapsedMs=${SystemClock.elapsedRealtime()}"
                )
            }
            // No delay when clearing search; short delay while typing to cut redundant DB work.
            .debounce(300)
            .onEach { q ->
                Log.d(
                    LOG_TAG,
                    "GeneralViewModel pipeline: after debounce len=${q.length} thread=${Thread.currentThread().name} elapsedMs=${SystemClock.elapsedRealtime()}"
                )
            }
            .distinctUntilChanged()
            .onEach { q ->
                Log.d(
                    LOG_TAG,
                    "GeneralViewModel pipeline: after distinct len=${q.length} elapsedMs=${SystemClock.elapsedRealtime()}"
                )
            }
            .flatMapLatest { query ->
                val t0 = SystemClock.elapsedRealtime()
                Log.d(
                    LOG_TAG,
                    "GeneralViewModel flatMapLatest START queryLen=${query.length} elapsedMs=$t0 thread=${Thread.currentThread().name}"
                )
                val flow = repository.getPagedPatients(query, generalTabDao)
                Log.d(
                    LOG_TAG,
                    "GeneralViewModel flatMapLatest repository.getPagedPatients returned +${SystemClock.elapsedRealtime() - t0}ms"
                )
                flow
            }
            .cachedIn(viewModelScope)
    }
}