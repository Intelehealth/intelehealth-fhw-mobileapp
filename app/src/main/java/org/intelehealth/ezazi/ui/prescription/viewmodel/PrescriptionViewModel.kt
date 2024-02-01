package org.intelehealth.ezazi.ui.prescription.viewmodel

import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewmodel.ViewModelInitializer
import org.intelehealth.ezazi.app.AppConstants
import org.intelehealth.ezazi.core.BaseViewModel
import org.intelehealth.ezazi.ui.prescription.data.PrescriptionRepository

/**
 * Created by Vaghela Mithun R. on 01-02-2024 - 00:33.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class PrescriptionViewModel(val repository: PrescriptionRepository) : BaseViewModel() {

    fun getPrescriptions(visitId: String) = executeLocalQuery {
        repository.fetchPrescription(visitId)
    }.asLiveData()

    companion object {
        val initializer = ViewModelInitializer(PrescriptionViewModel::class.java) {
            return@ViewModelInitializer PrescriptionRepository(AppConstants.inteleHealthDatabaseHelper.readableDatabase).let {
                return@let PrescriptionViewModel(it)
            }
        }
    }
}