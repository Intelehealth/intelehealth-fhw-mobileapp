package org.intelehealth.fcm.utils

import android.content.Context


/**
 * Created by Vaghela Mithu R. on 01-12-2022 - 18:32.
 * Email : vaghela@codeglo.com
 * Mob   : +919727206702
 **/
object FcmConstants {
    private const val FCM_BACKGROUND_BROADCAST_ACTION = "FCM_BACKGROUND_BROADCAST_ACTION"
    private const val FCM_FOREGROUND_BROADCAST_ACTION = "FCM_FOREGROUND_BROADCAST_ACTION"
    const val FCM_NOTIFICATION_PAYLOAD = "fcm_remote_message_notification_payload"
    const val FCM_DATA_PAYLOAD = "fcm_remote_message_data_payload"
    const val NOT_CHANNEL_NAME = "fcm_channel"
    const val NOTIFICATION_ID = "fcm_notification_id"
    const val FCM_TITLE_KEY = "title"
    const val FCM_BODY_KEY = "body"

    fun getBackgroundBroadcastAction(context: Context): String {
        return "${context.applicationContext.packageName}.$FCM_BACKGROUND_BROADCAST_ACTION"
    }

    fun getForegroundBroadcastAction(context: Context): String {
        return "${context.applicationContext.packageName}.$FCM_FOREGROUND_BROADCAST_ACTION"
    }
}