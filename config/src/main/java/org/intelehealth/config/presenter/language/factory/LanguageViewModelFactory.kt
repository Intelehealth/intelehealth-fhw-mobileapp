package org.intelehealth.config.presenter.language.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.intelehealth.config.presenter.fields.data.RegFieldRepository
import org.intelehealth.config.presenter.fields.viewmodel.RegFieldViewModel
import org.intelehealth.config.presenter.language.data.LanguageRepository
import org.intelehealth.config.presenter.language.viewmodel.LanguageViewModel

/**
 * Created by Vaghela Mithun R. on 15-04-2024 - 15:54.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class LanguageViewModelFactory(private val repository: LanguageRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LanguageViewModel(repository) as T
    }
}