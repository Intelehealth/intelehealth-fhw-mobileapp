package org.intelehealth.app.activities.notification.usecase

import org.intelehealth.app.activities.notification.repository.NotificationRepository

class UpdateNotificationStatusUseCase {
    private val notificationRepository = NotificationRepository()

   operator fun invoke(notificationId: String) =
        notificationRepository.updateNotificationStatus(notificationId)
}