package org.intelehealth.app.feature.video.utils

/**
 * Created by Vaghela Mithun R. on 19-09-2023 - 16:27.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
enum class CallStatus {
    MISSED, BUSY, ON_GOING, NONE;

    fun isMissed() = this == MISSED

    fun isBusy() = this == BUSY

    fun isOnGoing() = this == ON_GOING
}