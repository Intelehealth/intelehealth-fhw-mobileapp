package org.intelehealth.ezazi.ui.visit.viewmodel

import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.ViewModelInitializer
import org.intelehealth.ezazi.BuildConfig
import org.intelehealth.ezazi.app.AppConstants
import org.intelehealth.ezazi.core.BaseViewModel
import org.intelehealth.ezazi.networkApiCalls.ApiClient
import org.intelehealth.ezazi.networkApiCalls.ApiInterface
import org.intelehealth.ezazi.ui.password.data.ForgotPasswordRepository
import org.intelehealth.ezazi.ui.password.data.ForgotPasswordServiceDataSource
import org.intelehealth.ezazi.ui.password.viewmodel.PasswordViewModel
import org.intelehealth.ezazi.ui.visit.data.VisitRepository

/**
 * Created by Vaghela Mithun R. on 16-01-2024 - 19:40.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class VisitViewModel(private val visitRepository: VisitRepository) : BaseViewModel() {
    fun upcomingVisits() = executeLocalQuery {
        visitRepository.getAllUpcomingVisits()
    }.asLiveData()

    fun outcomePendingVisits() = executeLocalQuery {
        visitRepository.getAllUpcomingVisits()
    }.asLiveData()

    fun completedVisits(offset: Int, limit: Int, providerId: String) = executeLocalQuery {
        visitRepository.getCompletedVisits(offset, limit, providerId)
    }.asLiveData()

    companion object {
        val initializer = ViewModelInitializer(VisitViewModel::class.java) {
            return@ViewModelInitializer VisitRepository(AppConstants.inteleHealthDatabaseHelper.readableDatabase).let {
                return@let VisitViewModel(it)
            }
        }
    }

}