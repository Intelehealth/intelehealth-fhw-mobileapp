package org.intelehealth.app.feature.video.model

import android.os.Parcelable
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import org.intelehealth.app.feature.video.R
import org.intelehealth.app.feature.video.utils.CallAction
import org.intelehealth.app.feature.video.utils.CallMode
import org.intelehealth.app.feature.video.utils.CallStatus
import org.intelehealth.app.feature.video.utils.CallType
import org.json.JSONObject

/**
 * Created by Vaghela Mithun R. on 07-06-2023 - 17:42.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
@Parcelize
data class CallArgs(
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
    @SerializedName("connectToDrId")
    var doctorUuid: String? = null,
    var visitId: String? = null,
    var doctorId: String? = null,
    var appToken: String? = null,
    @SerializedName("device_token")
    var deviceToken: String? = "",
    var timestamp: Long = 0,
    var sound: Boolean = true,

    // call arguments
    var callType: CallType = CallType.NONE,
    var callStatus: CallStatus = CallStatus.NONE,
    var callAction: CallAction = CallAction.NONE,
    var callMode: CallMode = CallMode.NONE,
    var notificationChannelName: String? = null,
    var notificationChannelId: String? = null,
    var notificationId: Int = 0,
    var notificationIcon: Int = R.drawable.ic_call_accept,
    var notificationTime: String? = null
) : Parcelable {
    fun toJson(): String = Gson().toJson(this)

    fun isVideoCall() = callType.isVideoCall()

    fun isAudioCall() = callType.isAudioCall()

    fun isIncomingCall() = callMode.isIncomingCall()

    fun isOutGoingCall() = callMode.isOutGoingCall()

    fun isCallAccepted() = callAction.isAccepted()

    fun isCallDeclined() = callAction.isDeclined()

    fun isCallHangUp() = callAction.isHangUp()

    fun isCallOnGoing() = callStatus.isOnGoing()

    fun isMissedCall() = callStatus.isMissed()

    fun isBusyCall() = callStatus.isBusy()

    fun toJsonArg() = JSONObject().apply {
        put("patientId", patientId)
        put("connectToDrId", doctorUuid)
        put("visitId", visitId)
        put("nurseName", nurseName)
        put("patientName", patientName)
        put("patientPersonUuid", patientPersonUuid)
        put("patientOpenMrsId", patientOpenMrsId)
        put("token", token)
    }

    fun clone(): CallArgs = CallArgs(
        patientId = this.patientId,
        patientPersonUuid = this.patientPersonUuid,
        patientOpenMrsId = this.patientOpenMrsId,
        id = this.id,
        url = this.url,
        socketUrl = this.socketUrl,
        token = this.token,
        roomId = this.roomId,
        doctorName = this.doctorName,
        nurseName = this.nurseName,
        patientName = this.patientName,
        nurseId = this.nurseId,
        doctorUuid = this.doctorUuid,
        visitId = this.visitId,
        doctorId = this.doctorId,
        appToken = this.appToken,
        deviceToken = this.deviceToken,
        timestamp = this.timestamp,
        callType = this.callType,
        callStatus = this.callStatus,
        callAction = this.callAction,
        callMode = this.callMode,
        notificationChannelName = this.notificationChannelName,
        notificationChannelId = this.notificationChannelId,
        notificationId = this.notificationId,
        notificationIcon = this.notificationIcon,
        notificationTime = this.notificationTime
    )

    companion object {

        fun dummy(): CallArgs {
            val json = "{" +
                    "  \"actionType\": \"VIDEO_CALL\"," +
                    "  \"appToken\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ2aWRlbyI6eyJyb29tSm9pbiI6dHJ1ZSwicm9vbSI6ImM0MWNmMWU5LTMxNWUtNGFlNi05ZTIwLWNiNzJmZTMwZDg4MyIsImNhblB1Ymxpc2giOnRydWUsImNhblN1YnNjcmliZSI6dHJ1ZSwiZXhwIjoiMTAgZGF5cyJ9LCJpYXQiOjE2OTcyODk4MDksIm5iZiI6MTY5NzI4OTgwOSwiZXhwIjoxNjk3MzExNDA5LCJpc3MiOiJkZXZrZXkiLCJzdWIiOiJjMWVmYTQxZC0zYzQzLTQ1ZDYtOWUzYS00NzI4MjNiNjFhNTkiLCJqdGkiOiJjMWVmYTQxZC0zYzQzLTQ1ZDYtOWUzYS00NzI4MjNiNjFhNTkifQ.LTJJ_BF4c8hZo2Cfe7ItPnP5-GUXfqgQqYIkiSSiZG8\"," +
                    "  \"assignorNurseName\": \"\"," +
                    "  \"callEnded\": false," +
                    "  \"device_token\": \"\"," +
                    "  \"doctorId\": \"30cd7a4f-421e-45c1-b64f-392705fd4eae\"," +
                    "  \"doctorName\": \"Waghela Mithun Ravjibhai\"," +
                    "  \"id\": \"3d86222b-9645-4fff-af8a-fab8e772f8bf\"," +
                    "  \"isIncomingCall\": false," +
                    "  \"nurseId\": \"c1efa41d-3c43-45d6-9e3a-472823b61a59\"," +
                    "  \"nurseName\": \"mithunnurse\"," +
                    "  \"shift_change\": \"\"," +
                    "  \"patientUuid\": \"\"," +
                    "  \"providerID\": \"\"," +
                    "  \"roomId\": \"c41cf1e9-315e-4ae6-9e20-cb72fe30d883\"," +
                    "  \"socketUrl\": \"https://ezazi.intelehealth.org:3004?userId\\u003dc1efa41d-3c43-45d6-9e3a-472823b61a59\\u0026name\\u003dmithunnurse\"," +
                    "  \"tag\": \"\"," +
                    "  \"timestamp\": 1697289811433," +
                    "  \"url\": \"wss://ezazi.intelehealth.org:9090\"," +
                    "  \"visitId\": \"52b1d64c-5843-44e9-b042-8019263e151b\"," +
                    "  \"visitUuid\": \"\"" +
                    "}"
            return Gson().fromJson(json, CallArgs::class.java)
        }
    }
}