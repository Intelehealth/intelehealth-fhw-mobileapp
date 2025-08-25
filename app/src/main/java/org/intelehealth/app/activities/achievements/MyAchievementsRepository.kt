package org.intelehealth.app.activities.achievements

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.intelehealth.app.activities.achievements.dao.MyAchievementsDao
import org.intelehealth.app.database.InteleHealthDatabaseHelper

class MyAchievementsRepository(private val dbHelper: InteleHealthDatabaseHelper) {
    val dao = MyAchievementsDao(dbHelper)

    //For todays data
    fun getTodaysDoctorVisitsCount(creatorUuid: String, attributeTypeUuid: String, todaysDate: String): Int {
        return dao.getTodaysDoctorVisitsCount(creatorUuid, attributeTypeUuid, todaysDate)
    }

    fun getTodaysNCDVisitsCount(creatorUuid: String, attributeTypeUuid: String, todaysDate: String): Int {
        return dao.getTodaysNCDVisitsCount(creatorUuid, attributeTypeUuid, todaysDate)
    }

    fun getPatientsRegisteredTodayByLoggedInHw(creatorUuid: String, todaysDate: String): Int {
        return dao.getPatientsRegisteredTodayByLoggedInHw(creatorUuid, todaysDate)
    }
    fun getHWTodaysActiveStatus(creatorUuid: String, todaysDate: String): Boolean {
        return dao.getHWTodaysActiveStatus(creatorUuid, todaysDate)
    }

    fun getBaselineSurveyRegisteredTodaysPatients(creatorUuid: String, todaysDate: String): Int {
        return dao.getBaselineSurveyRegisteredTodaysPatients(creatorUuid, todaysDate)
    }

    //For date range data
    fun getDoctorVisitsCountInGivenDateRange(creatorUuid: String, attributeTypeUuid: String, startDate: String, endDate: String): Int {
        return dao.getDoctorVisitsCountInGivenDateRange(creatorUuid, attributeTypeUuid, startDate, endDate)
    }
    fun getNCDVisitsCountInGivenDateRange(creatorUuid: String, attributeTypeUuid: String, startDate: String, endDate: String): Int {
        return dao.getNCDVisitsCountInDateRange(creatorUuid, attributeTypeUuid, startDate, endDate)
    }

    fun getPatientsRegisteredByLoggedInHwInGivenDateRange(creatorUuid: String, startDate: String, endDate: String): Int {
        return dao.getPatientsRegisteredByLoggedInHwInDateRange(creatorUuid, startDate, endDate)
    }

    fun getHWActiveStatusInDateRange(creatorUuid: String, startDate: String, endDate: String): Int {
        return dao.getHWActiveStatusInDateRange(creatorUuid, startDate, endDate)
    }
    fun getBaselineSurveyRegisteredPatientsInDateRange(creatorUuid: String, startDate: String, endDate: String): Int {
        return dao.getBaselineSurveyRegisteredPatientsInDateRange(creatorUuid, startDate, endDate)
    }
}
