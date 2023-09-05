package org.intelehealth.klivekit.restapi

import org.intelehealth.klivekit.chat.model.ChatMessage
import org.intelehealth.klivekit.restapi.response.ChatResponse
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * Created by Vaghela Mithun R. on 30-08-2023 - 15:34.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
interface WebRtcApiClient {
    @POST("/api/messages/sendMessage")
    suspend fun sendMessage(message: ChatMessage): ChatResponse<List<ChatMessage>>

    @PUT("/api/messages/read/{messageId}")
    suspend fun markAsRead(@Path("messageId") messageId: Int)
}