package org.intelehealth.app.ui.baseline_survey.model

data class MedicalHistory(
    var hypertension: String = "-",
    var diabetes: String = "-",
    var arthritis: String = "-",
    var anemia: String = "-",
    var anySurgeries: String = "-",
    var reasonForSurgery: String = "-",
    var medicationForAnemia: String = "-",
    var healthWorkerForAnemia: String = "-",
    var reasonForNoAnemiaMedication: String = "-"
   /* val hypertension: String = "-",
    val diabetes: String = "-",
    val arthritis: String = "-",
    val anemia: String = "-",
    val anySurgeries: String = "-",
    val reasonForSurgery: String = "-"*/
)
