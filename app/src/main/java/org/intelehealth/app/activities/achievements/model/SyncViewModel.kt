package org.intelehealth.app.activities.achievements.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SyncViewModel : ViewModel() {

    private val _isSyncing = MutableLiveData<Boolean>()
    val isSyncing: LiveData<Boolean> get() = _isSyncing

    private val _syncResult = MutableLiveData<Boolean>()
    val syncResult: LiveData<Boolean> get() = _syncResult

    fun startSync() {
        _isSyncing.value = true

        // Do your sync operation (network/db)
        viewModelScope.launch {
            try {
                val result = doSync()
                _syncResult.value = result
            } finally {
                _isSyncing.value = false
            }
        }
    }

    private suspend fun doSync(): Boolean {
        delay(2000) // Simulate work
        return true // Or false on failure
    }
}
