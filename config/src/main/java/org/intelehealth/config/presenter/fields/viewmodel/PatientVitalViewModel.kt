package org.intelehealth.config.presenter.fields.viewmodel

import org.intelehealth.config.presenter.fields.data.PatientVitalRepository
import org.intelehealth.config.presenter.fields.data.RegFieldRepository
import org.intelehealth.config.utility.FieldGroup
import org.intelehealth.core.shared.ui.viewmodel.BaseViewModel

/**
 * Created by Lincon Pradhan R. on 24-06-2024 - 11:22.
 * Email : lincon@intelehealth.org
 * Mob   :
 **/
class PatientVitalViewModel(private val repository: PatientVitalRepository) : BaseViewModel() {

    fun getAllEnabledLiveFields() = repository.getAllEnabledLiveFields()
    suspend fun getAllEnabledFields() = repository.getAllEnabledFields()
}