package org.intelehealth.app.webrtc.service

import com.github.ajalt.timberkt.Timber
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import org.intelehealth.app.models.FollowUpNotificationData
import org.intelehealth.app.utilities.NotificationSchedulerUtils
import org.intelehealth.app.utilities.SessionManager
import org.intelehealth.app.webrtc.receiver.FCMNotificationReceiver
import org.intelehealth.fcm.FBMessageService
import org.intelehealth.klivekit.utils.FirebaseUtils

/**
 * Created by Vaghela Mithun R. on 18-09-2023 - 10:16.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class FCMMessageService : FBMessageService(FCMNotificationReceiver::class.java) {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Timber.d { "onNewToke ---> $token" }
        // save fcm reg. token for chat (Video)
        val sessionManager = SessionManager(this)
        FirebaseUtils.saveToken(
            this,
            sessionManager.providerID,
            token,
            sessionManager.appLanguage
        )
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Timber.d { "Remote message ${Gson().toJson(message)}" }
        NotificationSchedulerUtils.scheduleFollowUpNotification(FollowUpNotificationData(
            value = message.data["followupDatetime"] ?:"",
            name = message.data["patientFirstName"] +" "+ message.data["patientLastName"],
            openMrsId = message.data["followupDatetime"] ?:"",
            patientUid = message.data["patientUuid"] ?:"",
            visitUuid = message.data["patientOpenMrsId"] ?:"",
        ))
    }
}