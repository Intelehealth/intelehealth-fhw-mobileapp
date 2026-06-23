package org.intelehealth.app.activities.achievements.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.internal.Contexts.getApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.intelehealth.app.activities.achievements.MyAchievementsRepository
import org.intelehealth.app.activities.user.api.AppUsageTrackerNew
import org.intelehealth.app.app.IntelehealthApplication
import org.intelehealth.app.database.InteleHealthDatabaseHelper
import org.intelehealth.app.user.UserSessionDao
import org.intelehealth.app.utilities.SessionManager
import org.intelehealth.core.shared.ui.viewmodel.BaseViewModel
import org.intelehealth.klivekit.utils.DateTimeUtils.formatMillisToHourMinute
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DailyMyAchievementsViewModel(application: IntelehealthApplication) : BaseViewModel() {

    private val dbHelper = IntelehealthApplication.inteleHealthDatabaseHelper
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

    private val _averageSessionDuration = MutableLiveData<String?>()
    val averageSessionDuration: MutableLiveData<String?> get() = _averageSessionDuration


    private val context = application.applicationContext

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
   fun getUserAverageTimeSpentByDate(date: String) {
       viewModelScope.launch {
           val dbDurationMillis = withContext(Dispatchers.IO) {
               UserSessionDao(context)
                   .getAverageSessionDurationByDate(SessionManager(context).providerID, date)
           }

           // Get today's date in yyyy-MM-dd format for comparison
           val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

           val unsavedMillis = if (date == today) {
               AppUsageTrackerNew(context, SessionManager(context)).getUnsavedTime()
           } else { 0L }

           val totalDurationMillis = dbDurationMillis + unsavedMillis
           _averageSessionDuration.value = formatMillisToHourMinute(totalDurationMillis)
       }
   }


}
