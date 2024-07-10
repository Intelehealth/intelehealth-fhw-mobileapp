package org.intelehealth.app.ui.patient.fragment

import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import org.intelehealth.app.app.IntelehealthApplication
import org.intelehealth.app.database.dao.PatientsDAO
import org.intelehealth.app.ui.patient.data.PatientRepository
import org.intelehealth.app.ui.patient.viewmodel.PatientViewModel
import org.intelehealth.config.presenter.fields.factory.PatientViewModelFactory
import org.intelehealth.config.presenter.fields.viewmodel.RegFieldViewModel
import org.intelehealth.config.room.ConfigDatabase

/**
 * Created by Vaghela Mithun R. on 10-07-2024 - 10:56.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
abstract class BasePatientFragment(@LayoutRes layoutResId: Int) : Fragment(layoutResId) {
    protected val patientViewModel by lazy {
        return@lazy PatientViewModelFactory.create(requireActivity(), requireActivity())
    }
}