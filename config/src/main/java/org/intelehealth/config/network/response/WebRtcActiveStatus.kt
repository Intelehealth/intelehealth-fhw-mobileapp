package org.intelehealth.config.network.response

import com.google.gson.annotations.SerializedName

/**
 * Created by Vaghela Mithun R. on 29-05-2024 - 18:04.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
data class WebRtcActiveStatus(
    @SerializedName("chat")
    val chat: Boolean,
    @SerializedName("video_call")
    val video: Boolean
)
