package org.intelehealth.ncd.search.viewmodel

import android.content.res.Resources
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.intelehealth.ncd.R
import org.intelehealth.ncd.constants.Constants
import org.intelehealth.ncd.model.MedicalHistory
import org.intelehealth.ncd.model.Patient
import org.intelehealth.ncd.model.PatientAttributes
import org.intelehealth.ncd.data.SearchRepository

class AnemiaScreeningViewModel(
    private val repository: SearchRepository,
    private val resources: Resources
) : ViewModel() {

    private val _anemiaScreeningMutableLiveData: MutableLiveData<List<Patient>> = MutableLiveData()
    val anemiaScreeningLiveData = _anemiaScreeningMutableLiveData

    fun getPatientsForAnemiaScreening(age: Int) {
        val anemiaScreeningPatients: MutableList<Patient> = mutableListOf()

        viewModelScope.launch(Dispatchers.IO) {
            val patientsBasedOnAge = repository.getPatientsBasedOnAge(age)

            patientsBasedOnAge.forEach {
                val historyData: PatientAttributes = repository.getPatientsBasedOnUuids(
                    it.uuid,
                    Constants.OTHER_MEDICAL_HISTORY
                )

                if (doesPatientHaveAnemia(historyData.value)) {
                    anemiaScreeningPatients.add(it)
                }
            }

            _anemiaScreeningMutableLiveData.postValue(anemiaScreeningPatients)
        }
    }

    private fun doesPatientHaveAnemia(medicalHistoryJson: String?): Boolean {
        medicalHistoryJson?.let {
            val medicalHistoryDataList: List<MedicalHistory> = Gson().fromJson(
                medicalHistoryJson,
                object : TypeToken<List<MedicalHistory>>() {}.type
            )

            return medicalHistoryDataList[0].anaemia == resources.getString(R.string.medical_history_yes)
        }
        return false
    }
}