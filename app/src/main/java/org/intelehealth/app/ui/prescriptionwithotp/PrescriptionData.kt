package org.intelehealth.app.ui.prescriptionwithotp

import org.intelehealth.app.models.Patient

class PrescriptionData(
    val patient: Patient,
    val vitals:HashMap<String, String>?,
    val diagnostics:HashMap<String, String>?,
    val adultInitials: HashMap<String, String>?,
    val visitCompleteEncData: HashMap<String, String>?

)