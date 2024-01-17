package org.intelehealth.ezazi.ui.visit.viewmodel

import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewmodel.ViewModelInitializer
import org.intelehealth.ezazi.app.AppConstants
import org.intelehealth.ezazi.core.BaseViewModel
import org.intelehealth.ezazi.ui.visit.data.VisitRepository

/**
 * Created by Vaghela Mithun R. on 16-01-2024 - 19:40.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class VisitViewModel(private val visitRepository: VisitRepository) : BaseViewModel() {
    fun upcomingVisits() = executeLocalQuery {
        visitRepository.getUpcomingVisits()
    }.asLiveData()

    fun outcomePendingVisits(offset: Int, limit: Int, providerId: String) = executeLocalQuery {
        visitRepository.getOutcomePendingVisits(offset, limit, providerId)
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