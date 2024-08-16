package org.intelehealth.app.database.dao.notification

object NotificationDbConstants {

    // Table Name
    const val NOTIFICATION_TABLE = "tbl_notifications"

    // column name
    const val UUID =  "uuid"
    const val DESCRIPTION  =  "description"
    const val NOTIFICATION_TYPE  =  "notification_type"
    const val OBS_SERVER_MODIFIED_DATE  =  "obs_server_modified_date"
    const val IS_DELETED  =  "isdeleted"
    const val PRESCRIPTION_TYPE_NOTIFICATION: String = "prescription_type"
    const val FOLLOW_UP_NOTIFICATION: String = "follow_up_notification"
    const val READ_STATUS: String = "1"
    const val UN_READ_STATUS: String = "0"

}