package org.intelehealth.ncd.search.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.intelehealth.ncd.data.category.CategoryRepository
import org.intelehealth.ncd.category.viewmodel.AnemiaFollowUpViewModel
import org.intelehealth.ncd.category.viewmodel.AnemiaScreeningViewModel
import org.intelehealth.ncd.category.viewmodel.DiabetesFollowUpViewModel
import org.intelehealth.ncd.category.viewmodel.DiabetesScreeningViewModel
import org.intelehealth.ncd.category.viewmodel.GeneralViewModel
import org.intelehealth.ncd.category.viewmodel.HypertensionFollowUpViewModel
import org.intelehealth.ncd.category.viewmodel.HypertensionScreeningViewModel
import org.intelehealth.ncd.data.search.SearchRepository
import org.intelehealth.ncd.search.viewmodel.SearchViewModel
import org.intelehealth.ncd.utils.CategorySegregationUtils

@Suppress("UNCHECKED_CAST")
class SearchViewModelFactory(
    private val repository: SearchRepository,
    private val utils: CategorySegregationUtils
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {

            SearchViewModel::class.java -> SearchViewModel(repository, utils)

            else -> throw IllegalArgumentException("Unknown ViewModel class")

        } as T
    }
}