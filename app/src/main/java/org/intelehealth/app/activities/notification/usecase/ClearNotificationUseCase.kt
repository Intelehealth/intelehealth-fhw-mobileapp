package org.intelehealth.app.activities.notification.usecase

import org.intelehealth.app.activities.notification.repository.NotificationRepository

class ClearNotificationUseCase {
    private val notificationRepository = NotificationRepository()

    fun clearAllNotifications(userUid : String) = notificationRepository.clearAllNotification(userUid)

}