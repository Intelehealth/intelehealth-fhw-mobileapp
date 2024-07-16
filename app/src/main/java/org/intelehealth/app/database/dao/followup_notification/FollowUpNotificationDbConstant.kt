package org.intelehealth.app.database.dao.followup_notification

/**
 * Created By Tanvir Hasan on 7/2/24 5:25 AM
 * Email: tanvirhasan553@gmail.com
 */
object FollowUpNotificationDbConstant {
    const val TABLE = "tbl_follow_up_notification_schedule"

    //fields
    const val ID = "id"
    const val DATE_TIME = "date_time"//timestamp
    const val VALUE = "value"//actual date time
    const val DURATION = "duration"
    const val NAME = "name"
    const val OPENMRS_ID = "openmrs_id"
    const val PATIENT_UUID = "patient_uuid"
    const val VISIT_UUID = "visit_uuid"
    const val REQUEST_CODE = "request_code"
}