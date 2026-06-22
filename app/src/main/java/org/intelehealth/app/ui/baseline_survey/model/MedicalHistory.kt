package org.intelehealth.app.ui.baseline_survey.model

data class MedicalHistory(
    var bp: String = "-",
    var diabetes: String = "-",
    var arthritis: String = "-",
    var anemia: String = "-",
    var anaemia: String = "-",
    var anySurgeries: String = "-",
    var reasonForSurgery: String = "-",
    var medicationForAnemia: String = "-",
    var healthWorkerForAnemia: String = "-",
    var reasonForNoAnemiaMedication: String = "-",
    var medicationForBP: String = "-",
    var healthWorkerForBP: String = "-",
    var reasonForNoHypertensionMedication: String = "-",
    var hypertension: String = "-",
    var medicationForDiabetes: String = "-",
    var healthWorkerForDiabetes: String = "-",
    var reasonForNoDiabetesMedication: String = "-",

    /* val hypertension: String = "-",
     val diabetes: String = "-",
     val arthritis: String = "-",
     val anemia: String = "-",
     val anySurgeries: String = "-",
     val reasonForSurgery: String = "-"*/
)
