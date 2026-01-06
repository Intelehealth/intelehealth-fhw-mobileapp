package org.intelehealth.app.activities.visitSummaryActivity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.intelehealth.app.database.dao.VisitsDAO
import org.intelehealth.app.models.NCDReading

class VisitsSummaryViewModel: ViewModel() {
    private val _ncdReadings = MutableLiveData<List<NCDReading>>()
    val ncdReadings: LiveData<List<NCDReading>> = _ncdReadings

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadNcdVitals(patientUuid: String){
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val values = withContext(Dispatchers.IO) {
                    VisitsDAO.fetchObservationValues(patientUuid)
                }
                _ncdReadings.value = values
            } catch (e: Exception) {
                _error.value = "Error fetching observations: ${e.message}"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}