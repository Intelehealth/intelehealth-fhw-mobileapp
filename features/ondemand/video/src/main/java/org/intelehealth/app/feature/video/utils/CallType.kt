package org.intelehealth.app.feature.video.utils

/**
 * Created by Vaghela Mithun R. on 19-09-2023 - 16:19.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
enum class CallType {
    AUDIO, VIDEO, NONE;

    fun isAudioCall() = this == AUDIO

    fun isVideoCall() = this == VIDEO
}