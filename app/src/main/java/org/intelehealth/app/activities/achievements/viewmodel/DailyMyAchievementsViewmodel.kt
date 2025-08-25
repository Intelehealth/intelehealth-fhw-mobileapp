package org.intelehealth.app.activities.achievements.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.intelehealth.app.activities.achievements.MyAchievementsRepository
import org.intelehealth.app.app.IntelehealthApplication
import org.intelehealth.app.database.InteleHealthDatabaseHelper
import org.intelehealth.core.shared.ui.viewmodel.BaseViewModel

class DailyMyAchievementsViewModel(application: IntelehealthApplication) : BaseViewModel() {

    private val dbHelper = InteleHealthDatabaseHelper(application)
    private val repository = MyAchievementsRepository(dbHelper)

    private val _doctorVisitCount = MutableLiveData<Int>()
    val doctorVisitCount: LiveData<Int> get() = _doctorVisitCount

    private val _sevikaVisitCount = MutableLiveData<Int>()
    val sevikaVisitCount: LiveData<Int> get() = _sevikaVisitCount

    private val _patientsRegisteredTodayByLoggedInHw = MutableLiveData<Int>()
    val patientsRegisteredTodayByLoggedInHw: LiveData<Int> get() = _patientsRegisteredTodayByLoggedInHw

    private val _hwTodaysActiveStatus = MutableLiveData<Boolean>()
    val hwTodaysActiveStatus: LiveData<Boolean> get() = _hwTodaysActiveStatus

    private val _patientsWithBaselineSurvey = MutableLiveData<Int>()
    val patientsWithBaselineSurvey: LiveData<Int> get() = _patientsWithBaselineSurvey

    fun fetchTodaysDoctorVisits(creatorUuid: String, attributeTypeUuid: String, selectedDate: String) {
        viewModelScope.launch {
            val count = withContext(Dispatchers.IO) {
                repository.getTodaysDoctorVisitsCount(creatorUuid, attributeTypeUuid,selectedDate)
            }
            _doctorVisitCount.value = count
        }
    }

    fun fetchTodaysNCDVisits(creatorUuid: String, attributeTypeUuid: String, selectedDate: String) {
        viewModelScope.launch {
            val count = withContext(Dispatchers.IO) {
                repository.getTodaysNCDVisitsCount(creatorUuid, attributeTypeUuid, selectedDate)
            }
            _sevikaVisitCount.value = count
        }
    }

    fun fetchPatientsRegisteredTodayByLoggedInHw(creatorUuid: String, selectedDate: String) {
        viewModelScope.launch {
            val count = withContext(Dispatchers.IO) {
                repository.getPatientsRegisteredTodayByLoggedInHw(creatorUuid, selectedDate)
            }
            _patientsRegisteredTodayByLoggedInHw.value = count
        }
    }

    fun fetchHWTodaysActiveStatus(creatorUuid: String, selectedDate: String) {
        viewModelScope.launch {
            val count = withContext(Dispatchers.IO) {
                repository.getHWTodaysActiveStatus(creatorUuid, selectedDate)
            }
            _hwTodaysActiveStatus.value = count
        }
    }

    fun fetchBaselineSurveyRegisteredTodaysPatients(creatorUuid: String, selectedDate: String) {
        viewModelScope.launch {
            val count = withContext(Dispatchers.IO) {
                repository.getBaselineSurveyRegisteredTodaysPatients(creatorUuid, selectedDate)
            }
            _patientsWithBaselineSurvey.value = count
        }
    }

}
