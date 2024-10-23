package org.intelehealth.app.feature.video.utils

import org.intelehealth.app.feature.video.R


/**
 * Created by Vaghela Mithun R. on 05-07-2023 - 18:27.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
enum class CallMode(val value: String) {
    INCOMING("Incoming Call"), OUTGOING("Ongoing Call"), NONE("None");

    fun isIncomingCall() = this == INCOMING

    fun isOutGoingCall() = this == OUTGOING

    fun getResourceValue() = if (this == INCOMING) R.string.call_incoming
    else if (this == OUTGOING) R.string.call_outgoing
    else R.string.call_unknown
}