package org.intelehealth.config.presenter.section.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.intelehealth.config.presenter.section.data.ActiveSectionRepository
import org.intelehealth.config.presenter.section.viewmodel.ActiveSectionViewModel

/**
 * Created by Lincon Pradhan
 * Email : lincon@intelehealth.org
 **/
class ActiveSectionViewModelFactory(private val repository: ActiveSectionRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ActiveSectionViewModel(repository) as T
    }
}