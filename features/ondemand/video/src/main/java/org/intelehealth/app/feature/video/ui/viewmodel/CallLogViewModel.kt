package org.intelehealth.app.feature.video.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.intelehealth.app.feature.video.data.CallLogRepository

/**
 * Created by Vaghela Mithun R. on 23-10-2023 - 14:06.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class CallLogViewModel(private val repository: CallLogRepository) : ViewModel() {
    fun getCallLogs() = repository.getCallLogs()

    fun clearLogs() = viewModelScope.launch { repository.clearLogs() }
}