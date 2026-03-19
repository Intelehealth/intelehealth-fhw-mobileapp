package org.intelehealth.app.ui.patient.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import com.github.ajalt.timberkt.Timber
import com.google.gson.Gson
import org.intelehealth.abdm.model.AbhaProfileResponse
import org.intelehealth.abdm.model.OTPVerificationResponse
import org.intelehealth.abdm.utils.AbdmUtils
import org.intelehealth.app.models.dto.PatientDTO
import org.intelehealth.app.ui.patient.data.PatientRepository
import org.intelehealth.app.ui.rosterquestionnaire.utilities.FEMALE
import org.intelehealth.app.utilities.AbhaUtils
import org.intelehealth.app.utilities.DateAndTimeUtils
import org.intelehealth.app.utilities.PatientRegStage
import org.intelehealth.config.presenter.fields.viewmodel.RegFieldViewModel

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
    var activeStatusRosterSection = false

    var otpResponse: OTPVerificationResponse? = null
    var abhaResponse: AbhaProfileResponse? = null
    var xToken: String? = null
    var accessToken: String? = null

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
        return patient?.gender.equals(FEMALE, true) && DateAndTimeUtils.isDateGreaterThan15Years(
            patient?.dateofbirth
        )
    }

    fun getPatientDataFromOtpVerificationResponse(context: Context, patient: PatientDTO) {
        patient.also { patient ->
            otpResponse?.let { response ->
                AbhaUtils.getPatientPersonalDetailsFromOtpResponse(context, patient, response)
                AbhaUtils.getPatientAddressDetailsFromOtpResponse(patient, response)
                AbhaUtils.getPatientAbhaDetailsFromOtpResponse(patient, response)
            }
        }
    }

    fun getPatientDataFromAbhaProfileResponse(context: Context, patient: PatientDTO) {
        patient.also { patient ->
            abhaResponse?.let { response ->
                AbhaUtils.getPatientPersonalDetailsFromAbhaResponse(context, patient, response)
                AbhaUtils.getPatientAddressDetailsFromAbhaResponse(patient, response)
                AbhaUtils.getPatientAbhaDetailsFromAbhaResponse(patient, response)
            }
        }
    }
}