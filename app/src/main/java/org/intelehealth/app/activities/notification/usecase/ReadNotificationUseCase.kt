package org.intelehealth.app.activities.notification.usecase

import org.intelehealth.app.activities.notification.repository.NotificationRepository

class ReadNotificationUseCase {
    private val notificationRepository = NotificationRepository()

    fun fetchNonDeletedNotification() = notificationRepository.fetchNonDeletedNotification()

}