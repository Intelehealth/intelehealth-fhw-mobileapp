package org.intelehealth.klivekit.utils.extensions

import android.app.Notification
import android.app.Service
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.annotation.RequiresApi

/**
 * Created by Vaghela Mithun R. on 30-01-2024 - 18:11.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/

const val NO_SERVICE_TYPE = -1
fun Service.startSupportedForeground(
    serviceId: Int,
    notification: Notification,
    serviceType: Int = NO_SERVICE_TYPE
) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        startForeground(serviceId, notification)
    } else {
        val type = if (serviceType == NO_SERVICE_TYPE)
            ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL
        else serviceType

        startForeground(serviceId, notification, type)
    }
}