package org.intelehealth.ncd.category.viewmodel.factory

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
import org.intelehealth.ncd.utils.CategorySegregationUtils

@Suppress("UNCHECKED_CAST")
class CategoryViewModelFactory(
    private val repository: CategoryRepository,
    private val utils: CategorySegregationUtils
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {
            AnemiaScreeningViewModel::class.java -> AnemiaScreeningViewModel(repository, utils)

            AnemiaFollowUpViewModel::class.java -> AnemiaFollowUpViewModel(repository, utils)

            DiabetesScreeningViewModel::class.java -> DiabetesScreeningViewModel(repository, utils)

            DiabetesFollowUpViewModel::class.java -> DiabetesFollowUpViewModel(repository, utils)

            HypertensionScreeningViewModel::class.java -> HypertensionScreeningViewModel(
                repository,
                utils
            )

            HypertensionFollowUpViewModel::class.java -> HypertensionFollowUpViewModel(
                repository,
                utils
            )

            GeneralViewModel::class.java -> GeneralViewModel(repository)

            else -> throw IllegalArgumentException("Unknown ViewModel class")
        } as T
    }
}