package org.intelehealth.app.ui.patient.fragment

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import org.intelehealth.app.app.IntelehealthApplication
import org.intelehealth.app.database.dao.PatientsDAO
import org.intelehealth.app.models.dto.PatientDTO
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
    protected var patient: PatientDTO = PatientDTO()
    protected val patientViewModel by lazy {
        return@lazy PatientViewModelFactory.create(requireActivity(), requireActivity())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        patientViewModel.patientData.observe(viewLifecycleOwner) {
            if (it.cityvillage.isNullOrEmpty().not() && it.cityvillage.contains(":")) {
                patient.district = it.cityvillage.split(":")[0].trim()
            }
            onPatientDataLoaded(it)
        }
    }

    open fun onPatientDataLoaded(patient: PatientDTO) {
        this.patient = patient
    }

    /**
     * Whether this patient's identity fields came from a verified ABHA profile and must stay
     * read-only. Keyed on the record rather than on an intent extra, so it holds on every entry
     * path — fresh ABHA registration, and editing a linked patient later.
     *
     * "NA" counts as absent. The server returns that placeholder for a patient with no ABHA and the
     * pull stores it verbatim, so a blank-only check reported an ABHA for ordinary patients and locked
     * their name, phone and date of birth with nothing on screen to explain why.
     */
    protected fun hasAbha(): Boolean =
        patient.abhaNumber?.let { it.isNotBlank() && !it.equals("NA", ignoreCase = true) } ?: false
}