package org.intelehealth.klivekit.call.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.intelehealth.klivekit.call.utils.CallMode
import org.intelehealth.klivekit.call.utils.CallStatus
import org.intelehealth.klivekit.utils.DateTimeUtils.CALL_LOG_TIME_FORMAT
import org.intelehealth.klivekit.utils.extensions.milliToLogTime

/**
 * Created by Vaghela Mithun R. on 16-10-2023 - 11:51.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
@Entity(tableName = "call_log")
data class RtcCallLog(
    @PrimaryKey(autoGenerate = true)
    val callLogId: Long = 0,
    // Doctor Name
    var callerName: String = "",
    // Doctor Id
    var callerId: String = "",
    var callTime: String = "",
    // WebRtc Url
    var callUrl: String = "",
    // Patient Id
    var roomId: String = "",
    // Patient Name
    var roomName: String = "",
    // Nurse Id
    var calleeId: String = "",
    // Nurse name
    var calleeName: String = "",
    var callStatus: CallStatus = CallStatus.NONE,
    var callMode: CallMode = CallMode.NONE,
    var callAction: String = "",
    var chatAction: String = "",
    var hasChatAction: Boolean = false,
    var hasCallAction: Boolean = false
) {
    fun callLogTime(): String = callTime.milliToLogTime(CALL_LOG_TIME_FORMAT)
}