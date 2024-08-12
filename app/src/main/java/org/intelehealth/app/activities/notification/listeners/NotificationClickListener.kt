package org.intelehealth.app.activities.notification.listeners

import org.intelehealth.app.activities.notification.NotificationList
import org.intelehealth.app.models.NotificationModel

interface NotificationClickListener {
    fun deleteNotification(notificationModel: NotificationModel, position: Int)

    fun openNotification(notificationModel: NotificationModel, position: Int)
}
interface CloudNotificationClickListener {
    fun deleteNotification(notificationModel: NotificationList, position: Int)
    fun updateReadStatus(notificationModel: NotificationList, position: Int)

}