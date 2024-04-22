package org.intelehealth.app.activities.notification.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import org.intelehealth.app.activities.notification.result.NotificationResult
import org.intelehealth.app.activities.notification.usecase.DeleteNotificationUseCase
import org.intelehealth.app.activities.notification.usecase.ReadNotificationUseCase
import org.intelehealth.app.models.NotificationModel

class NotificationViewModel : ViewModel() {

    private var deleteNotificationUseCase = DeleteNotificationUseCase()
    private var readNotificationUseCase = ReadNotificationUseCase()

    fun fetchNonDeletedNotification(): MutableLiveData<NotificationResult<List<NotificationModel>>> {
        val notificationResult = MutableLiveData<NotificationResult<List<NotificationModel>>>()
        notificationResult.value = NotificationResult.Loading
        viewModelScope.launch(IO) {

            val notifications = readNotificationUseCase.fetchNonDeletedNotification()
            notificationResult.postValue(NotificationResult.Data(notifications))

        }
        return notificationResult

    }

    fun getPrescriptionCount() = readNotificationUseCase.fetchPrescriptionCount()

    fun deleteNotification(uuid: String) {
        deleteNotificationUseCase.deleteNotification(uuid)
    }

    fun deleteAllNotifications() {
        deleteNotificationUseCase.deleteAllNotifications()
    }

}