package org.intelehealth.ncd.callbacks

import org.intelehealth.ncd.model.Patient
import org.intelehealth.ncd.model.PatientVisitDetails

interface PatientClickedListener {
    fun onPatientClicked(patient: PatientVisitDetails)
}