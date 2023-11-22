package org.intelehealth.klivekit.model

import java.util.UUID

/**
 * Created by Vaghela Mithun R. on 13-07-2023 - 12:01.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class ActiveUser(
    val uuid: String? = null,
    val status: String? = null,
    val name: String? = null,
    val callStatus: String? = null,
    val room: String? = null,
    var sid: String? = null
){
    fun isOnline() = status == "online"

    fun isCalling() = callStatus == "calling"

}