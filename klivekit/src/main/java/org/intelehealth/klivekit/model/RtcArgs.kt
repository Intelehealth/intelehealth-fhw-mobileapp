package org.intelehealth.klivekit.model

import android.os.Parcelable
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import org.intelehealth.klivekit.call.utils.CallAction
import org.intelehealth.klivekit.call.utils.CallStatus
import org.intelehealth.klivekit.call.utils.CallType
import org.intelehealth.klivekit.call.utils.CallMode
import org.intelehealth.klivekit.utils.RemoteActionType

/**
 * Created by Vaghela Mithun R. on 07-06-2023 - 17:42.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
@Parcelize
data class RtcArgs(
    var patientId: String? = null,
    var patientPersonUuid: String? = null,
    var patientOpenMrsId: String? = null,
    var id: String? = null,
    var url: String? = null,
    var socketUrl: String? = null,
    var token: String? = null,
    var roomId: String? = null,
    var doctorName: String? = null,
    var nurseName: String? = null,
    var patientName: String? = null,
    var nurseId: String? = null,
    var visitId: String? = null,
    @SerializedName("connectToDrId")
    var doctorId: String? = null,
    var appToken: String? = null,
    var isIncomingCall: Boolean = false,
    @SerializedName("device_token")
    var deviceToken: String? = "",
    var actionType: String = RemoteActionType.TEXT_CHAT.name,
    var callEnded: Boolean = false,
    var timestamp: Long = 0,
    var callType: CallType = CallType.NONE,
    var callStatus: CallStatus = CallStatus.NONE,
    var callAction: CallAction = CallAction.NONE,
    var callMode: CallMode = CallMode.NONE
) : Parcelable {
    fun toJson(): String = Gson().toJson(this)

    fun isVideoCall() = callType == CallType.VIDEO

    fun isAudioCall() = callType == CallType.AUDIO

    fun isIncomingCall() = callMode == CallMode.INCOMING

    fun isOutGoingCall() = callMode == CallMode.OUTGOING

    fun isAcceptCall() = callAction == CallAction.ACCEPT

    fun isCallDeclined() = callAction == CallAction.DECLINE

    fun isCallOnGoing() = callStatus == CallStatus.ON_GOING

    fun isMissedCall() = callStatus == CallStatus.MISSED

    fun isBusyCall() = callStatus == CallStatus.BUSY
}