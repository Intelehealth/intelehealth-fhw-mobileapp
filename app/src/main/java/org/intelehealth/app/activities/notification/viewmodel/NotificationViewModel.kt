package org.intelehealth.app.activities.notification.viewmodel

import androidx.lifecycle.ViewModel
import org.intelehealth.app.activities.notification.usecase.DeleteNotificationUseCase
import org.intelehealth.app.activities.notification.usecase.ReadNotificationUseCase

class NotificationViewModel : ViewModel() {

    private var deleteNotificationUseCase = DeleteNotificationUseCase()
    private var readNotificationUseCase = ReadNotificationUseCase()

    fun fetchNonDeletedNotification() = readNotificationUseCase.fetchNonDeletedNotification()

    fun deleteNotification(uuid : String) {
        deleteNotificationUseCase.deleteNotification(uuid)
    }
    fun deleteAllNotifications() {
        deleteNotificationUseCase.deleteAllNotifications()
    }

}