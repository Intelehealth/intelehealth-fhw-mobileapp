package org.intelehealth.core.socket.user

/**
 * Created by Vaghela Mithun R. on 26-09-2024 - 13:51.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class OnlineUser(
    val uuid: String? = null,
    val status: String? = null,
    val name: String? = null,
    val callStatus: String? = null,
    val room: String? = null,
    var sid: String? = null
) {
    fun isOnline() = status == "online"

    fun isCalling() = callStatus == "calling"

}