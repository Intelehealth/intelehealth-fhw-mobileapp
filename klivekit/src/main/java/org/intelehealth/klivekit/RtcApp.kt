package org.intelehealth.klivekit

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import org.intelehealth.klivekit.chat.ChatClient
import org.intelehealth.klivekit.room.WebRtcDatabase
import org.intelehealth.klivekit.socket.SocketManager

/**
 * Created by Vaghela Mithun R. on 07-07-2023 - 19:54.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
open class RtcApp : Application() {
    companion object {
        lateinit var database: WebRtcDatabase
    }

    override fun onCreate() {
        super.onCreate()
        database = WebRtcDatabase.getInstance(this)
        ChatClient()
    }
}