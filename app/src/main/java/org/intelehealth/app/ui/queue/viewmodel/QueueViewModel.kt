package org.intelehealth.app.ui.queue.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import org.intelehealth.app.ui.queue.model.QueueRow
import org.intelehealth.app.ui.queue.repository.QueueRepository

/**
 * Exposes the patient queue to the screen as [LiveData]. Reads run on the IO
 * dispatcher via [viewModelScope]; the list survives configuration changes, so
 * the fragment loads once and just re-observes on recreation.
 */
class QueueViewModel(private val repository: QueueRepository) : ViewModel() {

    private val _queue = MutableLiveData<List<QueueRow>>()
    val queue: LiveData<List<QueueRow>> = _queue

    /** Loads the queue off the main thread and posts the result (empty on error). */
    fun loadQueue() {
        viewModelScope.launch(IO) {
            val rows = try {
                repository.getQueueList(DEFAULT_LIMIT, DEFAULT_OFFSET)
            } catch (e: Exception) {
                emptyList()
            }
            _queue.postValue(rows)
        }
    }

    companion object {
        // Matches the previous fragment constants (QUEUE_LIMIT / QUEUE_OFFSET).
        private const val DEFAULT_LIMIT = 50
        private const val DEFAULT_OFFSET = 0
    }
}
