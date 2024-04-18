package org.intelehealth.config.presenter.specialization.viewmodel

import org.intelehealth.config.presenter.specialization.data.SpecializationRepository
import org.intelehealth.core.shared.ui.viewmodel.BaseViewModel

/**
 * Created by Vaghela Mithun R. on 12-04-2024 - 12:56.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class SpecializationViewModel(private val repository: SpecializationRepository) : BaseViewModel() {
    fun fetchSpecialization() = repository.getAllLiveRecord()

    fun fetchSpecializationByName(name:String) = repository.getRecordByName(name)
}