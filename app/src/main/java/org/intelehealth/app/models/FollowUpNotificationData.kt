package org.intelehealth.app.models

/**
 * Created by Tanvir Hasan on 03-06-2024 : 18-17.
 * Email: mhasan@intelehealth.org
 */
data class FollowUpNotificationData(
    var patientUid: String,
    var openMrsId: String,
    var name: String,
    var gender: String,
    var encounterTypeUid: String,
    var visitUuid: String,
    var conceptUuid: String,
    var encounterUuid: String,
    var encounterUuidVitals: String,
    var value: String,
) {
    constructor(
        value: String,
        name: String,
        openMrsId: String,
        patientUid: String,
        visitUuid: String
    ) : this(
        patientUid, openMrsId, name,
        "", "", visitUuid,
        "", "", "",
        value
    )
}
