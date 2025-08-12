package org.intelehealth.app.models

data class PrescriptionDiagnosis(
    var diagnosis: String? = null,
    var type: String? = null,
    var tnm: String? = null,
    var otherStaging: String? = null
)
