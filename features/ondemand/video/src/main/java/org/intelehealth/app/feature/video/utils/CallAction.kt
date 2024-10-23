package org.intelehealth.app.feature.video.utils

/**
 * Created by Vaghela Mithun R. on 19-09-2023 - 16:19.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
enum class CallAction {
    ACCEPT, DECLINE, HANG_UP, NONE;

    fun isAccepted() = this == ACCEPT

    fun isDeclined() = this == DECLINE

    fun isHangUp() = this == HANG_UP

}