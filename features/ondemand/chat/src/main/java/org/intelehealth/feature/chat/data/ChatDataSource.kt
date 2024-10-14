package org.intelehealth.feature.chat.data

import org.intelehealth.feature.chat.model.ChatMessage
import org.intelehealth.feature.chat.restapi.WebRtcApiClient
import javax.inject.Inject

/**
 * Created by Vaghela Mithun R. on 03-07-2023 - 16:22.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class ChatDataSource @Inject constructor(private val restClient: WebRtcApiClient) {
    suspend fun sendMessage(message: ChatMessage) = restClient.sendMessage(message)
}