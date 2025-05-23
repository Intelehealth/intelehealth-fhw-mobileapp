package org.intelehealth.ncd.callbacks

import org.intelehealth.ncd.model.Patient

interface PatientClickedListener {
    fun onPatientClicked(patient: Patient)
}