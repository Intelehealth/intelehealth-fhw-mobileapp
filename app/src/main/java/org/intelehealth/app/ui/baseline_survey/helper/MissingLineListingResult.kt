package org.intelehealth.app.ui.baseline_survey.helper

data class MissingLineListingResult (
    val anemia: ComplaintStatus,
    val bp: ComplaintStatus,
    val diabetes: ComplaintStatus,
    val hasAnyHistoryWithoutMedication: Boolean
)
