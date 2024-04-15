package org.intelehealth.app.activities.notification.repository

import org.intelehealth.app.database.dao.EncounterDAO
import org.intelehealth.app.database.dao.VisitsDAO.allNotEndedVisits
import org.intelehealth.app.database.dao.VisitsDAO.recentNotEndedVisits
import org.intelehealth.app.database.dao.notification.NotificationDAO
import org.intelehealth.app.database.dao.notification.NotificationDbConstants
import org.intelehealth.app.models.NotificationModel
import org.intelehealth.app.syncModule.SyncUtils

class NotificationRepository {
    private val notificationDao = NotificationDAO()
    private val encounterDAO = EncounterDAO()
    private val syncUtils = SyncUtils()
    fun fetchNonDeletedNotification(): List<NotificationModel> {
        syncUtils.syncBackground()
        val allVisitList = recentNotEndedVisits()
        val notificationList = ArrayList<NotificationModel>()
        allVisitList.forEach {prescriptionModel ->
            val notificationModel = NotificationModel()
            notificationModel.uuid =  prescriptionModel.visitUuid
            notificationModel.description =  ""
            notificationModel.notification_type = NotificationDbConstants.PRESCRIPTION_TYPE_NOTIFICATION
            notificationModel.obs_server_modified_date = prescriptionModel.obsservermodifieddate
            notificationList.add(notificationModel)
        }
        notificationDao.insertNotifications(notificationList)

        return notificationDao.nonDeletedNotifications()
    }

    fun deleteNotification(uuid: String) = notificationDao.deleteNotification(uuid)

    fun deleteAllNotifications() = notificationDao.markAllNotificationsAsDeleted()

}