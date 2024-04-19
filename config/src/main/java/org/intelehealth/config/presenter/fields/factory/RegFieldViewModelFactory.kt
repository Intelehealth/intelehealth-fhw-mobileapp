package org.intelehealth.config.presenter.fields.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.intelehealth.config.presenter.fields.data.RegFieldRepository
import org.intelehealth.config.presenter.fields.viewmodel.RegFieldViewModel

/**
 * Created by Vaghela Mithun R. on 15-04-2024 - 15:54.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class RegFieldViewModelFactory(private val repository: RegFieldRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RegFieldViewModel(repository) as T
    }
}