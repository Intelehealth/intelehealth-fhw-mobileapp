package org.intelehealth.app.ui.queue.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import org.intelehealth.app.database.dao.QueueDAO
import org.intelehealth.app.ui.queue.repository.QueueRepository
import org.intelehealth.app.ui.queue.viewmodel.QueueViewModel

/**
 * Builds the QueueDAO -> QueueRepository -> QueueViewModel graph, mirroring the
 * factory pattern used by [org.intelehealth.app.ui.patient.factory] etc. Call
 * [create] from the fragment to obtain the ViewModel scoped to that owner.
 */
class QueueViewModelFactory(
    private val repository: QueueRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return QueueViewModel(repository) as T
    }

    companion object {
        @JvmStatic
        fun create(owner: ViewModelStoreOwner): QueueViewModel {
            val repository = QueueRepository(QueueDAO())
            val factory = QueueViewModelFactory(repository)
            return ViewModelProvider(owner, factory).get(QueueViewModel::class.java)
        }
    }
}
