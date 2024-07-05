package org.intelehealth.config.presenter.fields.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.intelehealth.config.presenter.fields.data.PatientVitalRepository
import org.intelehealth.config.presenter.fields.data.RegFieldRepository
import org.intelehealth.config.presenter.fields.viewmodel.PatientVitalViewModel
import org.intelehealth.config.presenter.fields.viewmodel.RegFieldViewModel

/**
 * Created by Lincon Pradhan R. on 24-06-2024 - 11:22.
 * Email : lincon@intelehealth.org
 * Mob   :
 **/
class PatientVitalViewModelFactory(private val repository: PatientVitalRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PatientVitalViewModel(repository) as T
    }
}