package org.intelehealth.app.activities.achievements.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
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

class DateRangeMyAchievementsViewModel (application: IntelehealthApplication) : BaseViewModel() {

    private val dbHelper = IntelehealthApplication.inteleHealthDatabaseHelper
    private val repository = MyAchievementsRepository(dbHelper)
    private val context = application.applicationContext

    private val _doctorVisitCountInDateRange = MutableLiveData<Int>()
    val doctorVisitCountInDateRange: LiveData<Int> get() = _doctorVisitCountInDateRange

    private val _sevikaVisitCountInDateRange = MutableLiveData<Int>()
    val sevikaVisitCountInDateRange: LiveData<Int> get() = _sevikaVisitCountInDateRange

    private val _patientsRegisteredByLoggedInHwInDateRange = MutableLiveData<Int>()
    val patientsRegisteredByLoggedInHwInDateRange: LiveData<Int> get() = _patientsRegisteredByLoggedInHwInDateRange

     private val _hwActiveStatusInDateRange = MutableLiveData<Int>()
     val hwActiveStatusInDateRange: LiveData<Int> get() = _hwActiveStatusInDateRange

    private val _patientsWithBaselineSurveyInDateRange = MutableLiveData<Int>()
    val patientsWithBaselineSurveyInDateRange: LiveData<Int> get() = _patientsWithBaselineSurveyInDateRange

    private val _averageSessionDuration = MutableLiveData<String?>()
    val averageSessionDuration: MutableLiveData<String?> get() = _averageSessionDuration


    fun fetchDoctorVisitsCountInGivenDateRange(creatorUuid: String, attributeTypeUuid: String, startDate: String, endDate: String) {
        viewModelScope.launch {
            val count = withContext(Dispatchers.IO) {
                repository.getDoctorVisitsCountInGivenDateRange(creatorUuid, attributeTypeUuid,startDate, endDate)
            }
            _doctorVisitCountInDateRange.value = count
        }
    }

    fun fetchNCDVisitsInGivenDateRange(creatorUuid: String, attributeTypeUuid: String, startDate: String, endDate: String) {
        viewModelScope.launch {
            val count = withContext(Dispatchers.IO) {
                repository.getNCDVisitsCountInGivenDateRange(creatorUuid, attributeTypeUuid, startDate, endDate)
            }
            _sevikaVisitCountInDateRange.value = count
        }
    }

    fun fetchPatientsRegisteredInGivenDateRangeByLoggedInHw(creatorUuid: String, startDate: String, endDate: String) {
         viewModelScope.launch {
             val count = withContext(Dispatchers.IO) {
                 repository.getPatientsRegisteredByLoggedInHwInGivenDateRange(creatorUuid, startDate, endDate)
             }
             _patientsRegisteredByLoggedInHwInDateRange.value = count
         }
     }

    fun fetchHWActiveStatusInRange(creatorUuid: String,startDate: String, endDate: String) {
       viewModelScope.launch {
           val count = withContext(Dispatchers.IO) {
               repository.getHWActiveStatusInDateRange(creatorUuid, startDate, endDate)
           }
           _hwActiveStatusInDateRange.value = count
       }
   }

    fun fetchBaselineSurveyRegisteredPatientsInDateRange(creatorUuid: String,startDate: String, endDate: String) {
        viewModelScope.launch {
            val count = withContext(Dispatchers.IO) {
                repository.getBaselineSurveyRegisteredPatientsInDateRange(creatorUuid, startDate, endDate)
            }
            _patientsWithBaselineSurveyInDateRange.value = count
        }
    }
    fun getUserAverageTimeSpentBetweenDates(startDate: String, endDate: String) {
        viewModelScope.launch {
            val dbDurationMillis = withContext(Dispatchers.IO) {
                UserSessionDao(context)
                    .getTotalSessionDurationByDateRange(
                        SessionManager(context).providerID,
                        startDate,
                        endDate
                    )
            }

            // Format today's date
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            // Only add unsaved time if the endDate is today
            val unsavedMillis = if (endDate == today) {
                AppUsageTrackerNew(context, SessionManager(context)).getUnsavedTime()
            } else { 0L }
            val totalDurationMillis = dbDurationMillis + unsavedMillis
            _averageSessionDuration.value =  formatMillisToHourMinute(totalDurationMillis)
        }
    }

}
