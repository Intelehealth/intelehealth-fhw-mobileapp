package org.intelehealth.ncd.search.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.intelehealth.ncd.data.SearchRepository
import org.intelehealth.ncd.search.viewmodel.AnemiaScreeningViewModel
import org.intelehealth.ncd.search.viewmodel.DiabetesScreeningViewModel
import org.intelehealth.ncd.search.viewmodel.HypertensionScreeningViewModel
import org.intelehealth.ncd.utils.CategorySegregationUtils

@Suppress("UNCHECKED_CAST")
class CategoryViewModelFactory(
    private val repository: SearchRepository,
    private val utils: CategorySegregationUtils
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {
            AnemiaScreeningViewModel::class.java -> AnemiaScreeningViewModel(repository, utils)
            DiabetesScreeningViewModel::class.java -> DiabetesScreeningViewModel(repository, utils)

            HypertensionScreeningViewModel::class.java -> HypertensionScreeningViewModel(
                repository,
                utils
            )

            else -> throw IllegalArgumentException("Unknown ViewModel class")
        } as T
    }
}