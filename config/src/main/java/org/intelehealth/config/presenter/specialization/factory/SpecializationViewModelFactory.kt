package org.intelehealth.config.presenter.language.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.intelehealth.config.presenter.specialization.data.SpecializationRepository
import org.intelehealth.config.presenter.specialization.viewmodel.SpecializationViewModel

/**
 * Created by Vaghela Mithun R. on 15-04-2024 - 15:54.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class SpecializationViewModelFactory(private val repository: SpecializationRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SpecializationViewModel(repository) as T
    }
}