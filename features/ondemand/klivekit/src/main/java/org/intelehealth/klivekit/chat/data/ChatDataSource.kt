package org.intelehealth.klivekit.chat.data

import org.intelehealth.klivekit.chat.model.ChatMessage
import org.intelehealth.klivekit.restapi.WebRtcApiClient

/**
 * Created by Vaghela Mithun R. on 03-07-2023 - 16:22.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class ChatDataSource (private val restClient: WebRtcApiClient) {
    suspend fun sendMessage(message: ChatMessage) = restClient.sendMessage(message)
}
