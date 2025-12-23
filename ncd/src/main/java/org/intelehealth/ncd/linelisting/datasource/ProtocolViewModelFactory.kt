package org.intelehealth.ncd.linelisting.datasource

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.intelehealth.ncd.linelisting.viewmodels.ProtocolScreenViewModel
import org.intelehealth.ncd.utils.CategorySegregationUtils

class ProtocolViewModelFactory(
    private val repository: PatientVisitRepository,
    private val categorySegregationUtils: CategorySegregationUtils
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProtocolScreenViewModel::class.java)) {
            return ProtocolScreenViewModel(repository, categorySegregationUtils) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
