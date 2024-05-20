package org.intelehealth.ncd.model

data class MedicalHistory(
    val anaemia: String,
    val anySurgeries: String,
    val arthritis: String,
    val diabetes: String,
    val hypertension: String,
    val reasonForSurgery: String,

    val medicationForBP: String,
    val healthWorkerForBP: String,
    val reasonForNoBPMedication: String,
    val medicationForDiabetes: String,
    val healthWorkerForDiabetes: String,
    val reasonForNoDiabetesMedication: String,
    val medicationForAnemia: String,
    val healthWorkerForAnemia: String,
    val reasonForNoAnemiaMedication: String
)