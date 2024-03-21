package org.intelehealth.ncd.model

data class MedicalHistory(
    val anaemia: String,
    val anySurgeries: String,
    val arthritis: String,
    val diabetes: String,
    val hypertension: String,
    val reasonForSurgery: String
)