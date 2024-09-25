package org.intelehealth.klivekit.socket

import io.socket.emitter.Emitter

/**
 * Created by Vaghela Mithun R. on 20-10-2023 - 12:40.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
interface SocketEventListener {
    fun onEvent(event: String): Emitter.Listener
}