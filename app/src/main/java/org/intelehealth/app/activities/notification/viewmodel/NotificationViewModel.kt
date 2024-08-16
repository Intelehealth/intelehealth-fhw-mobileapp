package org.intelehealth.app.activities.notification.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import org.intelehealth.app.activities.notification.NotificationList
import org.intelehealth.app.activities.notification.NotificationResponse
import org.intelehealth.app.activities.notification.result.NotificationResult
import org.intelehealth.app.activities.notification.usecase.ClearNotificationUseCase
import org.intelehealth.app.activities.notification.usecase.DeleteLocalNotificationUseCase
import org.intelehealth.app.activities.notification.usecase.FetchAllLocalNotification
import org.intelehealth.app.activities.notification.usecase.FetchAllNotificationUseCase
import org.intelehealth.app.activities.notification.usecase.UpdateNotificationStatusUseCase
import org.intelehealth.app.models.NotificationModel
import retrofit2.Response
import timber.log.Timber

class NotificationViewModel : ViewModel() {

    private var deleteLocalNotificationUseCase = DeleteLocalNotificationUseCase()
    private val fetchAllLocalNotification = FetchAllLocalNotification()
    private val clearNotificationUseCase = ClearNotificationUseCase()
    private val fetchAllNotificationUseCase = FetchAllNotificationUseCase()
    private val updateNotificationStatusUseCase = UpdateNotificationStatusUseCase()

    fun fetchNonDeletedNotification(): MutableLiveData<NotificationResult<List<NotificationModel>>> {
        val notificationResult = MutableLiveData<NotificationResult<List<NotificationModel>>>()
        notificationResult.value = NotificationResult.Loading
        viewModelScope.launch(IO) {

            val notifications = fetchAllLocalNotification.fetchNonDeletedNotification()
            notificationResult.postValue(NotificationResult.Data(notifications))

        }
        return notificationResult
    }

    fun getPrescriptionCount() = fetchAllLocalNotification.fetchPrescriptionCount()

    fun deleteNotification(uuid: String) {
        deleteLocalNotificationUseCase.deleteNotification(uuid)
    }

    fun clearAllCloudNotification(uuid: String): MutableLiveData<NotificationResult<List<NotificationModel>>> {
        val notificationResult = MutableLiveData<NotificationResult<List<NotificationModel>>>()
        notificationResult.value = NotificationResult.Loading
        clearNotificationUseCase.clearAllNotifications(uuid).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : DisposableSingleObserver<ResponseBody>() {
                override fun onSuccess(responseBody: ResponseBody) {
                    Timber.tag("TAG").d("Notification Status update")
                }

                override fun onError(e: Throwable) {
                    Timber.tag("TAG").d("Notification Status Update failed")
                    e.printStackTrace()
                }
            })
        return notificationResult

    }
    fun updateNotificationStatus(notificationId: String)  {

        updateNotificationStatusUseCase(notificationId).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : DisposableSingleObserver<ResponseBody>() {
                override fun onSuccess(responseBody: ResponseBody) {
                    Timber.tag("TAG").d("Notification Status update")
                }

                override fun onError(e: Throwable) {
                    Timber.tag("TAG").d("Notification Status Update failed")
                    e.printStackTrace()
                }
            })


    }

    fun fetchAllCloudNotification(
        userId: String,
        page: String,
        size: String
    ): MutableLiveData<NotificationResult<List<NotificationList>>> {
        val notificationResult = MutableLiveData<NotificationResult<List<NotificationList>>>()
        notificationResult.value = NotificationResult.Loading

        fetchAllNotificationUseCase(userId = userId, page = page, size = size).subscribeOn(
            Schedulers.io()
        )
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : DisposableSingleObserver<NotificationResponse>() {
                override fun onSuccess(responseBody: NotificationResponse) {
                    val notificationList = responseBody.rows
                    notificationResult.postValue(NotificationResult.Data(notificationList))
                }

                override fun onError(e: Throwable) {
                    Timber.tag("TAG").d("Notification Status Update failed")
                    notificationResult.postValue(
                        NotificationResult.Error(
                            e.printStackTrace().toString()
                        )
                    )

                }
            })
        return notificationResult

    }


    fun deleteAllNotifications() {
        deleteLocalNotificationUseCase.deleteAllNotifications()
    }


}