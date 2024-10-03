package org.intelehealth.config.presenter.feature.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.intelehealth.config.presenter.feature.data.FeatureActiveStatusRepository
import org.intelehealth.config.presenter.feature.viewmodel.FeatureActiveStatusViewModel

/**
 * Created by Vaghela Mithun R. on 15-04-2024 - 15:54.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class FeatureActiveStatusViewModelFactory(private val repository: FeatureActiveStatusRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return FeatureActiveStatusViewModel(repository) as T
    }
}