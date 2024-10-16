package org.intelehealth.features.ondemand.mediator.utils

import android.content.Context
import android.content.Intent
import org.intelehealth.features.ondemand.mediator.VIDEO_CALL_IMPL_CLASS
import org.intelehealth.features.ondemand.mediator.createInstance
import org.intelehealth.features.ondemand.mediator.listener.VideoCallListener

/**
 * Created by Vaghela Mithun R. on 10-10-2024 - 11:37.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
object OnDemandIntentUtils {
    fun getChatIntent(): Intent {
        return Intent()
    }

    @JvmStatic
    fun startCallLog(context: Context?) {
        val listener = createInstance<VideoCallListener>(VIDEO_CALL_IMPL_CLASS)
        listener?.startCallLogActivity(context)
    }
}