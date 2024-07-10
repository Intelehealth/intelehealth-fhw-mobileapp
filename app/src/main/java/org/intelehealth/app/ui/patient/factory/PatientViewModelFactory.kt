package org.intelehealth.config.presenter.fields.factory

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import org.intelehealth.app.app.IntelehealthApplication
import org.intelehealth.app.database.dao.PatientsDAO
import org.intelehealth.app.ui.patient.data.PatientRepository
import org.intelehealth.app.ui.patient.viewmodel.PatientViewModel
import org.intelehealth.config.presenter.fields.data.RegFieldRepository
import org.intelehealth.config.presenter.fields.viewmodel.RegFieldViewModel
import org.intelehealth.config.room.ConfigDatabase

/**
 * Created by Vaghela Mithun R. on 15-04-2024 - 15:54.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class PatientViewModelFactory(
    private val patientRepository: PatientRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PatientViewModel(patientRepository) as T
    }

    companion object {
        fun create(
            context: Context,
            owner: ViewModelStoreOwner
        ): PatientViewModel {
            val configDb = ConfigDatabase.getInstance(context)
            val patientDao = PatientsDAO()
            val sqlHelper = IntelehealthApplication.inteleHealthDatabaseHelper
            val repository = PatientRepository(patientDao, sqlHelper, configDb.patientRegFieldDao())
            val factory = PatientViewModelFactory(repository)
            return ViewModelProvider(owner, factory)[PatientViewModel::class]
        }
    }
}