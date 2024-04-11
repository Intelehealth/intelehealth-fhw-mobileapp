package org.intelehealth.app.activities.notification.repository

import org.intelehealth.app.models.NotificationModel

class NotificationRepository {
    fun fetchNonDeletedNotification() : List<NotificationModel> {
        val notificationList = ArrayList<NotificationModel>()
        notificationList.add(NotificationModel())
        notificationList.add(NotificationModel())
        notificationList.add(NotificationModel())
        notificationList.add(NotificationModel())
        return notificationList
    }

    fun deleteNotification(uuid: String) {

    }

    fun deleteAllNotifications() {

    }
}