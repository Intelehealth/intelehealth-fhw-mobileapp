package org.intelehealth.klivekit

import android.app.Application
import org.intelehealth.klivekit.socket.SocketManager

/**
 * Created by Vaghela Mithun R. on 07-07-2023 - 19:54.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
open class RtcApp : Application() {
    protected val socketManager = SocketManager()
    override fun onCreate() {
        super.onCreate()
    }

    override fun onTerminate() {
        super.onTerminate()
    }
}