package org.intelehealth.app.ui.baseline_survey.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import org.intelehealth.app.ui.baseline_survey.data.BaselineRepository
import org.intelehealth.app.ui.baseline_survey.model.Baseline
import org.intelehealth.app.ui.patient.data.PatientRepository
import org.intelehealth.app.utilities.BaselineSurveyStage
import org.intelehealth.config.presenter.fields.viewmodel.RegFieldViewModel

/**
 * Created by Shazzad H Kanon on 10-12-2024 - 11:00.
 * Email : shazzad@intelehealth.org
 * Mob   : +8801647040520
 **/
class BaselineSurveyViewModel(
    private val repository: PatientRepository,
    private val baselineRepository: BaselineRepository
) : RegFieldViewModel(repository) {

    private var baselineMutableLiveData = MutableLiveData<Baseline>()
    val baselineData: LiveData<Baseline> get() = baselineMutableLiveData

    private var mutableBaselineSurveyStage = MutableLiveData(BaselineSurveyStage.GENERAL)
    val mutableBaselineSurveyStageData: LiveData<BaselineSurveyStage> get() = mutableBaselineSurveyStage

    var baselineEditMode: Boolean = false
    lateinit var patientId: String


    var baselineActiveMedicalSection = true
    var baselineActiveOtherSection = true
    var isEditMode: Boolean = false

    fun loadBaselineData(
        patientId: String
    ) = executeLocalQuery {
        baselineRepository.getPatientAttributes(patientId)
    }.asLiveData()

    fun updateBaselineData(baselineData: Baseline) {
        baselineMutableLiveData.postValue(baselineData)
    }

    fun updateBaselineStage(stage: BaselineSurveyStage) {
        mutableBaselineSurveyStage.postValue(stage)
    }

    fun getPatientAge(patientId: String) = executeLocalQuery {
        return@executeLocalQuery baselineRepository.getPatientAge(patientId)
    }.asLiveData()

    fun householdHasAnotherHead(patientId: String): Boolean =
        baselineRepository.householdHasAnotherHead(patientId)

    fun savePatient() = executeLocalInsertUpdateQuery {
        return@executeLocalInsertUpdateQuery baselineData.value?.let {
            baselineRepository.createPatientAttributes(it, patientId)
        } ?: false
    }.asLiveData()
}