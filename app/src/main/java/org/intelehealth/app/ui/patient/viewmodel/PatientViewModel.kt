package org.intelehealth.app.ui.patient.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import com.github.ajalt.timberkt.Timber
import com.google.gson.Gson
import org.intelehealth.app.models.dto.PatientDTO
import org.intelehealth.app.ui.patient.data.PatientRepository
import org.intelehealth.app.ui.rosterquestionnaire.utilities.FEMALE
import org.intelehealth.app.utilities.DateAndTimeUtils
import org.intelehealth.app.utilities.PatientRegStage
import org.intelehealth.config.presenter.fields.viewmodel.RegFieldViewModel
import org.intelehealth.klivekit.utils.Constants

/**
 * Created by Vaghela Mithun R. on 02-07-2024 - 13:49.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class PatientViewModel(
    private val repository: PatientRepository,
) : RegFieldViewModel(repository) {

    private var mutableLivePatient = MutableLiveData<PatientDTO>()
    val patientData: LiveData<PatientDTO> get() = mutableLivePatient
    private var mutableLivePatientStage = MutableLiveData(PatientRegStage.PERSONAL)
    val patientStageData: LiveData<PatientRegStage> get() = mutableLivePatientStage
    var activeStatusAddressSection = true
    var activeStatusOtherSection = true
    var isEditMode: Boolean = false

    /**
     * Set when registration was opened from the ABDM flow. Such a run is an edit — the ABHA resolved to
     * an existing patient, so the save must update rather than insert — but it still has to walk all
     * three stages, unlike the per-section edits launched from Patient Details.
     *
     * So [isEditMode] keeps deciding update-versus-insert, while this decides navigation. Field locking
     * stays on [isEditMode] alone: the admin config's non-editable fields must not become writable just
     * because the flow entered from ABDM.
     */
    var isAbhaFullFlow: Boolean = false
    var activeStatusRosterSection = false

    fun loadPatientDetails(
        patientId: String,
    ) = executeLocalQuery {
        repository.fetchPatient(patientId)
    }.asLiveData()

    fun updatedPatient(patient: PatientDTO) {
        Timber.d { "Saved patient => ${Gson().toJson(patient)}" }
        mutableLivePatient.postValue(patient)
    }

    fun updatePatientStage(stage: PatientRegStage) {
        mutableLivePatientStage.postValue(stage)
    }

    fun savePatient() = executeLocalInsertUpdateQuery {
        return@executeLocalInsertUpdateQuery patientData.value?.let {
            return@let if (isEditMode) repository.updatePatient(it)
            else repository.createNewPatient(it) // TODO: check with mithun this is creating a new record again with parent ID.
        } ?: false
    }.asLiveData()

    private val _addressInfoConfigCityVillageEnabled = MutableLiveData<Boolean>()
    val addressInfoConfigCityVillageEnabled: LiveData<Boolean> get() = _addressInfoConfigCityVillageEnabled
    fun setCityVillageEnabled(enabled: Boolean) {
        _addressInfoConfigCityVillageEnabled.value = enabled
    }

    fun getPregnancyVisibility(): Boolean {
        val patient = patientData.value
        return patient?.gender.equals(FEMALE,true) && DateAndTimeUtils.isDateGreaterThan15Years(patient?.dateofbirth)
    }
}