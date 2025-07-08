package org.intelehealth.config.presenter.section.viewmodel

import org.intelehealth.config.presenter.fields.data.RegFieldRepository
import org.intelehealth.config.presenter.language.data.LanguageRepository
import org.intelehealth.config.presenter.section.data.ActiveSectionRepository
import org.intelehealth.core.shared.ui.viewmodel.BaseViewModel

/**
 * Created by Lincon Pradhan
 * Email : lincon@intelehealth.org
 **/
class ActiveSectionViewModel(private val repository: ActiveSectionRepository) : BaseViewModel() {
    fun fetchActiveSection() = repository.getAllActiveSection()
}