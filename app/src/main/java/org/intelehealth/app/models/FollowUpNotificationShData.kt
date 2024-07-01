package org.intelehealth.app.models

/**
 * Created By Tanvir Hasan on 6/4/24 12:15 AM
 * Email: tanvirhasan553@gmail.com
 */
data class FollowUpNotificationShData(var id: String, var dateTime: String, var name: String, var openMrsId: String, var patientUid: String, var visitUuid: String) {
    constructor(name: String, id: String) : this(id = "", dateTime="", name="", openMrsId="", patientUid="", visitUuid="")
}
