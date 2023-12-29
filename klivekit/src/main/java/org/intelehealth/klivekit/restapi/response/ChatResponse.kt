package org.intelehealth.klivekit.restapi.response

import org.intelehealth.klivekit.model.ChatMessage

/**
 * Created by Vaghela Mithun R. on 30-08-2023 - 15:39.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class ChatResponse<T>(
    var success: Boolean = false,
    val data: T? = null
)