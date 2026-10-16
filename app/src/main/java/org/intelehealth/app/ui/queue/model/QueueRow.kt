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
    // Visit the queue row belongs to; used to resolve the chief complaint from
    // the visit's obs (the queue service does not send the complaint itself).
    val visitUuid: String?,
    // Chief complaint blob resolved from the visit's obs (not from tbl_queue),
    // parsed into symptom tags the same way the visit summary screen does.
    val chiefComplaint: String?,
    val waitedMinutes: Int,
    val etaMinutes: Int,
    // ISO-8601 instant the patient is expected to be seen (server "etaAt");
    // used to show the live wait time for next/waiting rows.
    val etaAt: String?,
    // ISO-8601 instant the call was connected (server "connectedAt"); used to
    // show the elapsed call duration for onCall rows.
    val connectedAt: String?,
    val patientPhoto: String?
)
