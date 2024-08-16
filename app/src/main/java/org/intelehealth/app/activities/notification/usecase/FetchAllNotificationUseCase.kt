package org.intelehealth.app.activities.notification.usecase

import org.intelehealth.app.activities.notification.repository.NotificationRepository

class FetchAllNotificationUseCase {
    private val notificationRepository = NotificationRepository()

    operator fun invoke(userId: String, page: String, size: String) =
        notificationRepository.fetchAllNotification(userId = userId, page = page, size = size)
}