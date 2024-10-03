package org.intelehealth.app.activities.notification.usecase

import org.intelehealth.app.activities.notification.repository.NotificationRepository

class DeleteLocalNotificationUseCase {

    private val notificationRepository = NotificationRepository()
    fun deleteNotification(uuid: String) {
        notificationRepository.deleteNotification(uuid)
    }

    fun deleteAllNotifications() {
        notificationRepository.deleteAllNotifications()
    }
}