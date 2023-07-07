package org.intelehealth.klivekit.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.ajalt.timberkt.Timber
import com.google.gson.Gson
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.intelehealth.klivekit.model.RtcArgs
import org.intelehealth.klivekit.socket.SocketManager
import org.intelehealth.klivekit.utils.extensions.hide
import org.json.JSONObject

/**
 * Created by Vaghela Mithun R. on 08-06-2023 - 20:17.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class SocketViewModel(private val args: RtcArgs) : ViewModel() {
    private val socketManager = SocketManager(args.socketUrl)

    private val mutableEventAudioOnOff = MutableLiveData(true)
    val eventAudioOnOff = mutableEventAudioOnOff.hide()

    private val mutableEventBye = MutableLiveData(false)
    val eventBye = mutableEventBye.hide()

    private val mutableIncomingCAll = MutableLiveData(false)
    val eventCall = mutableIncomingCAll.hide()

    private val mutableEventCreated = MutableLiveData(false)
    val eventCreated = mutableEventCreated.hide()

    private val mutableEventFull = MutableLiveData(true)
    val eventFull = mutableEventFull.hide()

    private val mutableEventIpAddress = MutableLiveData(true)
    val eventIpAddress = mutableEventIpAddress.hide()

    private val mutableEventIsRead = MutableLiveData(false)
    val eventIsRead = mutableEventIsRead.hide()

    private val mutableEventJoin = MutableLiveData(false)
    val eventJoin = mutableEventJoin.hide()

    private val mutableEventJoined = MutableLiveData(true)
    val eventJoined = mutableEventJoined.hide()

    private val mutableEventLog = MutableLiveData(true)
    val eventLog = mutableEventLog.hide()

    private val mutableEventMessage = MutableLiveData<Any?>()
    val eventMessage = mutableEventMessage.hide()

    private val mutableEventNoAnswer = MutableLiveData(false)
    val eventNoAnswer = mutableEventNoAnswer.hide()

    private val mutableEventReady = MutableLiveData(false)
    val eventReady = mutableEventReady.hide()

    private val mutableEventUpdateMessage = MutableLiveData<Any?>()
    val eventUpdateMessage = mutableEventUpdateMessage.hide()

    private val mutableEventVideoOnOff = MutableLiveData(true)
    val eventVideoOnOff = mutableEventVideoOnOff.hide()

    private val mutableSocketConnected = MutableLiveData(false)
    val eventSocketConnected = mutableSocketConnected.hide()

    private val mutableSocketDisconnected = MutableLiveData(false)
    val eventSocketDisconnected = mutableSocketDisconnected.hide()

    private fun emitter(event: String) = Emitter.Listener {
        when (event) {
            SocketManager.EVENT_AUDIO_OFF -> updateAudioStatus(it, SocketManager.EVENT_AUDIO_OFF)
            SocketManager.EVENT_AUDIO_ON -> updateAudioStatus(it, SocketManager.EVENT_AUDIO_ON)
            SocketManager.EVENT_BYE -> sayByeToWeb()
            SocketManager.EVENT_CALL -> connectWithDoctor()
            SocketManager.EVENT_CREATED -> executeInUIThread { mutableEventCreated.postValue(true) }
            SocketManager.EVENT_FULL -> {}
            SocketManager.EVENT_IP_ADDRESS -> {}
            SocketManager.EVENT_IS_READ -> executeInUIThread { mutableEventIsRead.postValue(true) }
            SocketManager.EVENT_JOIN -> executeInUIThread { mutableEventJoin.postValue(true) }
            SocketManager.EVENT_JOINED -> executeInUIThread { mutableEventJoined.postValue(true) }
            SocketManager.EVENT_LOG -> {}
            SocketManager.EVENT_MESSAGE -> executeInUIThread { mutableEventMessage.postValue(it) }
            SocketManager.EVENT_NO_ANSWER -> sayByeToWeb()
            SocketManager.EVENT_READY -> executeInUIThread { mutableEventReady.postValue(true) }
            SocketManager.EVENT_UPDATE_MESSAGE -> executeInUIThread {
                mutableEventUpdateMessage.postValue(it)
            }

            SocketManager.EVENT_VIDEO_OFF -> updateVideoStatus(it, SocketManager.EVENT_VIDEO_OFF)
            SocketManager.EVENT_VIDEO_ON -> updateVideoStatus(it, SocketManager.EVENT_VIDEO_ON)
            Socket.EVENT_CONNECT -> connected(it)
            Socket.EVENT_DISCONNECT -> executeInUIThread { mutableSocketDisconnected.postValue(true) }
        }
    }

    fun connect() {
        socketManager.connect(this::emitter)
    }

    private fun connected(status: Array<Any>) {
        Timber.e { "Socket connected => ${Gson().toJson(status)}" }
        mutableSocketConnected.postValue(true)
        if (args.isIncomingCall.not())
            connectWithDoctor()
    }

    private fun connectWithDoctor() {
        executeInUIThread {
            emit(SocketManager.EVENT_CREATE_OR_JOIN_HW, Gson().toJson(args))
//            emit(SocketManager.EVENT_CREATE_OR_JOIN_HW, JSONObject().apply {
//                put("patientId", args.patientId)
//                put("connectToDrId", args.doctorUuid)
//                put("visitId", args.visitId)
//                put("nurseName", args.nurseName)
//                put("patientName", args.patientName)
//                put("patientPersonUuid", args.patientPersonUuid)
//                put("patientOpenMrsId", args.patientOpenMrsId)
//                put("token", args.token)
//            })
        }
    }

    private fun sayByeToWeb() {
        executeInUIThread {
            mutableEventBye.postValue(true)
            emit("bye")
        }
    }

    private fun updateAudioStatus(it: Array<Any>?, event: String) {
        it ?: return
        executeInUIThread {
            val jsonObject = JSONObject(it[0].toString())
            val flag = jsonObject.getBoolean("fromWebapp")
            if (flag && event == SocketManager.EVENT_AUDIO_OFF) {
                mutableEventAudioOnOff.postValue(false)
            } else if (flag && event == SocketManager.EVENT_AUDIO_ON) {
                mutableEventAudioOnOff.postValue(true)
            } else {
                mutableEventAudioOnOff.postValue(true)
            }
        }
    }

    private fun updateVideoStatus(it: Array<Any>?, event: String) {
        it ?: return
        executeInUIThread {
            val jsonObject = JSONObject(it[0].toString())
            val flag = jsonObject.getBoolean("fromWebapp")
            if (flag && event == SocketManager.EVENT_VIDEO_OFF) {
                mutableEventVideoOnOff.postValue(false)
            } else if (flag && event == SocketManager.EVENT_VIDEO_ON) {
                mutableEventVideoOnOff.postValue(true)
            } else {
                mutableEventVideoOnOff.postValue(true)
            }
        }
    }

    private fun executeInUIThread(block: () -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.Main) {
                block.invoke()
            }
        }
    }

    fun emit(event: String, args: Any? = null) = socketManager.emit(event, args)

    fun disconnect() = socketManager.disconnect()

    fun isConnected() = socketManager.isConnected()

    fun reconnect() = socketManager.reconnect()

    override fun onCleared() {
        super.onCleared()
        disconnect()
    }

}