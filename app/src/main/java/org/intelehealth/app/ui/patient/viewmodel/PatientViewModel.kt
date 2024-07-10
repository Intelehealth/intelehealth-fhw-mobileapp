package org.intelehealth.app.ui.patient.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.flow.collect
import org.intelehealth.app.models.dto.PatientDTO
import org.intelehealth.app.ui.patient.data.PatientRepository
import org.intelehealth.config.presenter.fields.data.RegFieldRepository
import org.intelehealth.config.presenter.fields.viewmodel.RegFieldViewModel
import org.intelehealth.core.shared.ui.viewmodel.BaseViewModel

/**
 * Created by Vaghela Mithun R. on 02-07-2024 - 13:49.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class PatientViewModel(
    private val repository: PatientRepository
) : RegFieldViewModel(repository) {

    private var mutableLivePatient = MutableLiveData<PatientDTO>()
    val patientData: LiveData<PatientDTO> get() = mutableLivePatient

    var activeStatusAddressSection = true
    var activeStatusOtherSection = true

    fun loadPatientDetails(
        patientId: String
    ) = executeLocalQuery {
        repository.fetchPatient(patientId)
    }.asLiveData()

    fun updatedPatient(patient: PatientDTO) {
        mutableLivePatient.postValue(patient)
    }

    fun savePatient() {
        patientData.value?.let {
            repository.createNewPatient(it)
        }
    }

}