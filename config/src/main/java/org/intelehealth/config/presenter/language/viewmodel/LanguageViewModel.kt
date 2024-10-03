package org.intelehealth.config.presenter.language.viewmodel

import org.intelehealth.config.presenter.fields.data.RegFieldRepository
import org.intelehealth.config.presenter.language.data.LanguageRepository
import org.intelehealth.core.ui.viewmodel.BaseViewModel

/**
 * Created by Vaghela Mithun R. on 12-04-2024 - 12:56.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class LanguageViewModel(private val repository: LanguageRepository) : BaseViewModel() {
    fun fetchSupportedLanguage() = repository.getAllSupportedLanguage()
}