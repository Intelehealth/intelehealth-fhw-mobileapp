package org.intelehealth.app.activities.notification.result

sealed class NotificationResult<out T> {
    data object Loading : NotificationResult<Nothing>()
    data class Data<T>(val data: T) : NotificationResult<T>()
}
