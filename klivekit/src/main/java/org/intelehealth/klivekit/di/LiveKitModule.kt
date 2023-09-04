package org.intelehealth.klivekit.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.livekit.android.LiveKit
import io.livekit.android.LiveKitOverrides
import io.livekit.android.RoomOptions
import io.livekit.android.audio.AudioSwitchHandler
import io.livekit.android.room.Room
import io.livekit.android.room.participant.AudioTrackPublishDefaults
import io.livekit.android.room.participant.VideoTrackPublishDefaults
import io.livekit.android.room.track.CameraPosition
import io.livekit.android.room.track.LocalAudioTrackOptions
import io.livekit.android.room.track.LocalVideoTrackOptions
import io.livekit.android.room.track.VideoPreset169
import org.intelehealth.klivekit.httpclient.OkHttpClientProvider
import org.webrtc.EglBase
import org.webrtc.HardwareVideoEncoderFactory
import javax.inject.Singleton

/**
 * Created by Vaghela Mithun R. on 04-09-2023 - 11:05.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
@InstallIn(SingletonComponent::class)
@Module
class LiveKitModule {
    @Singleton
    @Provides
    fun provideLocalAudioTrackOptions() = LocalAudioTrackOptions(
        noiseSuppression = true,
        echoCancellation = true,
        autoGainControl = true,
        highPassFilter = true,
        typingNoiseDetection = true,
    )

    @Singleton
    @Provides
    fun provideLocalVideoTrackOptions() = LocalVideoTrackOptions(
        deviceId = "",
        position = CameraPosition.FRONT,
        captureParams = VideoPreset169.QVGA.capture,
    )

    @Singleton
    @Provides
    fun provideAudioPublishDefault() = AudioTrackPublishDefaults(
        audioBitrate = 20_000,
        dtx = true,
    )

    @Singleton
    @Provides
    fun provideVideoPublishTrack() = VideoTrackPublishDefaults(
        videoEncoding = VideoPreset169.QVGA.encoding,
    )

    @Singleton
    @Provides
    fun provideRoomOptions(
        localAudioTrackOptions: LocalAudioTrackOptions,
        localVideoTrackOptions: LocalVideoTrackOptions,
        audioTrackPublishDefaults: AudioTrackPublishDefaults,
        videoTrackPublishDefaults: VideoTrackPublishDefaults
    ) = RoomOptions(
        audioTrackCaptureDefaults = localAudioTrackOptions,
        audioTrackPublishDefaults = audioTrackPublishDefaults,
        adaptiveStream = true
    )

    @Singleton
    @Provides
    fun provideAudioSwitchHandler(@ApplicationContext context: Context) =
        AudioSwitchHandler(context)

    @Singleton
    @Provides
    fun provideLiveKitRoom(
        @ApplicationContext context: Context,
        options: RoomOptions, audioSwitchHandler: AudioSwitchHandler
    ): Room = LiveKit.create(
        appContext = context,
        options = options,
        overrides = LiveKitOverrides(
            okHttpClient = OkHttpClientProvider().provideOkHttpClient(),
            audioHandler = audioSwitchHandler,
            videoEncoderFactory = HardwareVideoEncoderFactory(
                EglBase.create().eglBaseContext,
                false,
                true
            )
        )
    )
}