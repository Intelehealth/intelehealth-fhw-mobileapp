package org.intelehealth.app.activities.notification.repository

import android.annotation.SuppressLint
import io.reactivex.Single
import okhttp3.ResponseBody
import org.intelehealth.app.BuildConfig
import org.intelehealth.app.activities.notification.NotificationResponse
import org.intelehealth.app.app.IntelehealthApplication
import org.intelehealth.app.database.dao.VisitsDAO
import org.intelehealth.app.database.dao.VisitsDAO.recentVisits
import org.intelehealth.app.database.dao.notification.NotificationDAO
import org.intelehealth.app.database.dao.notification.NotificationDbConstants
import org.intelehealth.app.models.NotificationModel
import org.intelehealth.app.networkApiCalls.ApiClient
import org.intelehealth.app.networkApiCalls.ApiInterface
import org.intelehealth.app.syncModule.SyncUtils
import org.intelehealth.app.utilities.DateAndTimeUtils
import org.intelehealth.app.utilities.SessionManager
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

const val LIMIT = 15
const val OFFSET = 0
const val TAG = " NotificationRepository"

@SuppressLint("TimberTagLength")
class NotificationRepository {
    private val notificationDao = NotificationDAO()
    private val syncUtils = SyncUtils()
    private val apiService = ApiClient.createService(ApiInterface::class.java)
    private var sessionManager =
        SessionManager(IntelehealthApplication.getAppContext())

    init {
        ApiClient.changeApiBaseUrl(BuildConfig.SOCKET_URL)
    }

    fun updateNotificationStatus(id: String): Single<ResponseBody> =
        apiService.notificationsAcknowledge(
            "Basic " + sessionManager.encoded, id
        )

    fun clearAllNotification(userUid: String): Single<ResponseBody> =
        apiService.clearAllNotifications(
            "Basic " + sessionManager.encoded, userUid
        )

    fun fetchAllNotification(userId: String, page: String, size: String): Single<NotificationResponse> =
        apiService.fetchAllNotifications(
            "Basic " + sessionManager.encoded, userId, page, size
        )


    fun fetchNonDeletedNotification(): List<NotificationModel> {
        syncUtils.syncInBackground()
        val allVisitList = VisitsDAO.recentNotEndedVisits(LIMIT, OFFSET)
        val notificationList = ArrayList<NotificationModel>()

        allVisitList.forEach { prescriptionModel ->
            val notificationModel = NotificationModel().apply {
                uuid = prescriptionModel.visitUuid
                description =
                    "${prescriptionModel.first_name} ${prescriptionModel.last_name}'s prescription was received!"
                notification_type = NotificationDbConstants.PRESCRIPTION_TYPE_NOTIFICATION
                obs_server_modified_date = prescriptionModel.visit_start_date
            }
            notificationList.add(notificationModel)
        }

        notificationDao.insertNotifications(notificationList)

        val nonDeletedNotificationList = notificationDao.nonDeletedNotifications()
        val notificationListWithOutExpiredFollowup = ArrayList<NotificationModel>()

        nonDeletedNotificationList.forEach { notificationModel ->
            allVisitList.find { visitItem ->
                visitItem.visitUuid == notificationModel.uuid
            }?.let { visitItem ->
                notificationModel.apply {
                    first_name = visitItem.first_name
                    last_name = visitItem.last_name
                    patientuuid = visitItem.patientUuid
                    patient_photo = visitItem.patient_photo
                    visitUUID = visitItem.visitUuid
                    visit_startDate = visitItem.visit_start_date
                    gender = visitItem.gender
                    encounterUuidVitals = visitItem.encounterUuid
                    encounterUuidAdultIntial = visitItem.encounterUuid
                    date_of_birth = visitItem.dob
                    age = visitItem.dob
                    followupDate = visitItem.followup_date
                    openmrsID = visitItem.openmrs_id
                }
            }
        }

        val comparator = Comparator<NotificationModel> { notification1, notification2 ->
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault())
            try {
                // Parse the dates
                val date1 = format.parse(notification1.obs_server_modified_date)
                val date2 = format.parse(notification2.obs_server_modified_date)
                // Compare the dates
                date2!!.compareTo(date1) // Change to date1.compareTo(date2) for ascending order
            } catch (e: ParseException) {
                // Handle parse exception
                e.printStackTrace()
                0 // Return 0 if unable to parse, can be adjusted based on requirements
            }
        }

        /**
         * deleting all expired followup notification here
         * and creating new list
         */
        for (data in nonDeletedNotificationList) {
            if (data.notification_type == NotificationDbConstants.PRESCRIPTION_TYPE_NOTIFICATION) {
                notificationListWithOutExpiredFollowup.add(data)
            } else if (data.notification_type == NotificationDbConstants.FOLLOW_UP_NOTIFICATION &&
                DateAndTimeUtils.getTimeStampFromString(
                    data.description.substring(
                        data.description.length - 21,
                        data.description.length
                    ), "yyyy-MM-dd 'at' h:mm a"
                ) > System.currentTimeMillis()
            ) {
                notificationListWithOutExpiredFollowup.add(data)
            }
        }

        // Sort the notificationList ArrayList using the defined Comparator
        notificationListWithOutExpiredFollowup.sortWith(comparator)

        return notificationListWithOutExpiredFollowup
    }

    fun fetchPrescriptionCount() = recentVisits(LIMIT, OFFSET).size


    fun deleteNotification(uuid: String) = notificationDao.deleteNotification(uuid)

    fun deleteAllNotifications() = notificationDao.markAllNotificationsAsDeleted()


}