package org.intelehealth.app.activities.achievements.viewmodel

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

class DateRangeMyAchievementsViewModel (application: IntelehealthApplication) : BaseViewModel() {

    private val dbHelper = InteleHealthDatabaseHelper(application)
    private val repository = MyAchievementsRepository(dbHelper)

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


    /*fun fetchBaselineSurveyRegisteredTodaysPatients(creatorUuid: String, todaysDate: String) {
       viewModelScope.launch {
           val count = withContext(Dispatchers.IO) {
               repository.getBaselineSurveyRegisteredTodaysPatients(creatorUuid, todaysDate)
           }
           _patientsWithBaselineSurvey.value = count
       }
   }
*/
}
