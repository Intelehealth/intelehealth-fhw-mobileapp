package org.intelehealth.feature.chat.restapi

import org.intelehealth.feature.chat.model.ChatMessage
import org.intelehealth.feature.chat.restapi.response.ChatResponse
import retrofit2.http.POST

/**
 * Created by Vaghela Mithun R. on 30-08-2023 - 15:34.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
interface WebRtcApiClient {
    @POST("/api/messages/sendMessage")
    suspend fun sendMessage(message: ChatMessage): ChatResponse<List<ChatMessage>>
}