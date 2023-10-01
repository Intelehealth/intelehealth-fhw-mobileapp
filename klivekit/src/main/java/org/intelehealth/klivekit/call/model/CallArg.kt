package org.intelehealth.klivekit.call.model

import android.content.Context
import com.google.gson.Gson
import org.intelehealth.klivekit.model.RtcArgs
import org.intelehealth.klivekit.utils.extensions.fromJson

/**
 * Created by Vaghela Mithun R. on 30-09-2023 - 23:09.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
data class CallArg<T>(
    val context: Context,
    val data: T,
    val clazz: Class<*>,
    val notificationChannelName: String,
    val notificationChannelId: String,
)