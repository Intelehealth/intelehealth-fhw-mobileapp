package org.intelehealth.app.ui.patient.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import com.github.ajalt.timberkt.Timber
import com.google.gson.Gson
import org.intelehealth.abdm.constants.AbdmConstant
import org.intelehealth.abdm.model.OTPVerificationResponse
import org.intelehealth.app.models.Patient
import org.intelehealth.app.models.dto.PatientDTO
import org.intelehealth.app.ui.patient.data.PatientRepository
import org.intelehealth.app.ui.rosterquestionnaire.utilities.FEMALE
import org.intelehealth.app.utilities.DateAndTimeUtils
import org.intelehealth.app.utilities.PatientRegStage
import org.intelehealth.config.presenter.fields.viewmodel.RegFieldViewModel
import org.intelehealth.klivekit.utils.DateTimeUtils
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date

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

    var otpVerificationResponse: OTPVerificationResponse? = null

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

    fun getPatientFromOtpVerificationResponse(): PatientDTO {
        return PatientDTO().also { patient ->
            otpVerificationResponse?.let { response ->
                getPatientPersonalDetails(patient, response)
                getPatientAddressDetails(patient, response)
                getPatientAbhaDetails(patient, response)
            }
        }
    }

    private fun getPatientPersonalDetails(
        patient: PatientDTO,
        response: OTPVerificationResponse
    ) {
        patient.patientPhoto = response.abhaProfile.photo
        patient.firstname = response.abhaProfile.firstName
        patient.middlename = response.abhaProfile.middleName
        patient.lastname = response.abhaProfile.lastName
        patient.gender = response.abhaProfile.gender
        patient.phonenumber = "91${response.abhaProfile.mobile}"
        patient.dateofbirth = DateTimeUtils.formatToLocalDate(
            formatPatientDob(response.abhaProfile.dob),
            DateTimeUtils.YYYY_MM_DD_HYPHEN
        )
    }

    private fun getPatientAddressDetails(
        patient: PatientDTO,
        response: OTPVerificationResponse
    ) {
        patient.postalcode = response.abhaProfile.pinCode
        bifurcateAddress(response.abhaProfile.address, patient)
    }

    private fun getPatientAbhaDetails(
        patient: PatientDTO,
        response: OTPVerificationResponse
    ) {
        patient.abhaNumber = response.abhaProfile.abhaNumber
        patient.abhaAddress = "${response.abhaProfile.phrAddress[0]}"
    }

    private fun formatPatientDob(date: String): Date {
        val pattern = AbdmConstant.ABHA_DOB_FORMAT
        val localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern(pattern))
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
    }

    private fun bifurcateAddress(address: String, patientDTO: PatientDTO) {
        val parts = address.split(",").map { it.trim() }
        if (parts.size < 3) {
            patientDTO.address1 = address
            return
        }

        patientDTO.stateprovince = parts[parts.size - 1]
        patientDTO.district = parts[parts.size - 2]
        patientDTO.cityvillage = parts[parts.size - 3]
        patientDTO.address1 = parts.dropLast(3).joinToString(", ")
    }
}