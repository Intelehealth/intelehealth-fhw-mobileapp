package org.intelehealth.klivekit.ui.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.ajalt.timberkt.Timber
import io.livekit.android.LiveKit
import io.livekit.android.events.RoomEvent
import io.livekit.android.events.collect
import io.livekit.android.room.Room
import io.livekit.android.room.track.CameraPosition
import io.livekit.android.room.track.LocalVideoTrack
import io.livekit.android.room.track.Track
import io.livekit.android.room.track.VideoTrack
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.intelehealth.klivekit.utils.extensions.hide

/**
 * Created by Vaghela Mithun R. on 08-06-2023 - 15:59.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
open class CoreVideoViewModel(
    private val context: Context,
    private val url: String,
    private val token: String
) : ViewModel() {

    protected val mutableError = MutableStateFlow<Throwable?>(null)
    val error = mutableError.hide()

    // Controls
    private val mutableMicEnabled = MutableLiveData(true)
    val micEnabled = mutableMicEnabled.hide()

    private val mutableCameraEnabled = MutableLiveData(true)
    val cameraEnabled = mutableCameraEnabled.hide()

    private val mutableRemoteVideoTrack = MutableLiveData<VideoTrack?>()
    val remoteVideoTrack = mutableRemoteVideoTrack.hide()

    lateinit var room: Room

    open fun initRoom() {
        room = LiveKit.create(appContext = context)
    }

    open fun initRoomEvents() {
        viewModelScope.launch {
            room.events.collect { event ->
                when (event) {
                    is RoomEvent.TrackSubscribed -> onTrackSubscribed(event)
                    is RoomEvent.ParticipantDisconnected -> onParticipantDisconnected(event)
                    else -> {}
                }
            }
        }
    }

    private fun onTrackSubscribed(event: RoomEvent.TrackSubscribed) {
        val track = event.track
        if (track is VideoTrack) {
            mutableRemoteVideoTrack.postValue(track)
        }
    }

    private fun onParticipantDisconnected(event: RoomEvent.ParticipantDisconnected){
//        event.participant.sid == room.remoteParticipants.values.firstOrNull().sid
    }

    suspend fun connectToRoom() {
        try {
            room.connect(
                url = url,
                token = token,
            )

            // Create and publish audio/video tracks
            val localParticipant = room.localParticipant
            localParticipant.setMicrophoneEnabled(true)
            mutableMicEnabled.postValue(localParticipant.isMicrophoneEnabled())

            localParticipant.setCameraEnabled(true)
            mutableCameraEnabled.postValue(localParticipant.isCameraEnabled())

            setupRemoteVideoTrack()

        } catch (e: Throwable) {
            mutableError.value = e
        }
    }

    private fun configLocalTrack(){

    }

    open fun setupRemoteVideoTrack() {
        // Attach video of remote participant if already available.
        val remoteVideoTrack = room.remoteParticipants.values.firstOrNull()
            ?.getTrackPublication(Track.Source.CAMERA)
            ?.track as? VideoTrack

        if (remoteVideoTrack != null) {
            mutableRemoteVideoTrack.postValue(remoteVideoTrack)
        }
    }

    fun toggleMicrophone() {
        viewModelScope.launch {
            val enabled = room.localParticipant.isMicrophoneEnabled().not()
            room.localParticipant.setMicrophoneEnabled(enabled)
            mutableMicEnabled.postValue(enabled)
        }
    }

    fun toggleCamera() {
        viewModelScope.launch {
            val enabled = room.localParticipant.isCameraEnabled().not()
            room.localParticipant.setCameraEnabled(enabled)
            mutableCameraEnabled.postValue(enabled)
        }
    }

    fun flipCamera() {
        val videoTrack = room.localParticipant.getTrackPublication(Track.Source.CAMERA)
            ?.track as? LocalVideoTrack
            ?: return

        val newPosition = when (videoTrack.options.position) {
            CameraPosition.FRONT -> CameraPosition.BACK
            CameraPosition.BACK -> CameraPosition.FRONT
            else -> null
        }

        videoTrack.switchCamera(position = newPosition)
    }

    fun dismissError() {
        mutableError.value = null
    }

    open fun reconnect() {
        Timber.e { "Reconnecting." }
        room.disconnect()
        viewModelScope.launch {
            connectToRoom()
        }
    }

    override fun onCleared() {
        super.onCleared()
        disconnect()
    }

    fun disconnect() {
        room.disconnect()
        room.release()
    }
}