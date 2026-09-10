package org.intelehealth.app.ui.queue.model

/**
 * UI-facing model for one row of the patient queue screen: the queue fields
 * plus the patient display fields, already joined in a single query by
 * [org.intelehealth.app.ui.queue.repository.QueueRepository]. Presentation
 * shaping (status label, age, symptom split, time formatting) is applied by the
 * fragment when it converts this into the React Native prop bundle.
 */
data class QueueRow(
    val openmrsId: String?,
    val patientName: String?,
    val gender: String?,
    val dateOfBirth: String?,
    val status: String?,
    val position: Int,
    val chiefComplaint: String?,
    val waitedMinutes: Int,
    val etaMinutes: Int
)
