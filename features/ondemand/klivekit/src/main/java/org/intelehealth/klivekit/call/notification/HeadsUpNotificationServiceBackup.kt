package org.intelehealth.klivekit.call.notification

import android.app.Service
import android.content.Intent
import android.os.IBinder


/**
 * Created by Vaghela Mithun R. on 18-09-2023 - 10:34.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class HeadsUpNotificationServiceBackup : Service() {
//    private val CHANNEL_ID: String = "CallChannel"
//    private val CHANNEL_NAME: String = "Call Channel"
//    var mediaPlayer: MediaPlayer? = null
//    var mvibrator: Vibrator? = null
//    var audioManager: AudioManager? = null
//    var playbackAttributes: AudioAttributes? = null
//    private var handler: Handler? = null
//    var afChangeListener: OnAudioFocusChangeListener? = null
//    private var status = false
//    private var vstatus = false
//
    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }
//
//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        NotificationHandlerUtils.getIncomingNotificationBuilder()
//
//
//
//
//        NotificationHandlerUtils.notifyMissedCall()
//
//
//        var data: Bundle? = null
//        var name: String? = ""
//        var callType = ""
//        val NOTIFICATION_ID = 120
//        try {
//            audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
//            if (audioManager != null) {
//                when (audioManager!!.ringerMode) {
//                    AudioManager.RINGER_MODE_NORMAL -> status = true
//                    AudioManager.RINGER_MODE_SILENT -> status = false
//                    AudioManager.RINGER_MODE_VIBRATE -> {
//                        status = false
//                        vstatus = true
//                        Log.e("Service!!", "vibrate mode")
//                    }
//                }
//            }
//            if (status) {
//                val delayedStopRunnable = Runnable { releaseMediaPlayer() }
//                afChangeListener = OnAudioFocusChangeListener { focusChange ->
//                    if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
//                        // Permanent loss of audio focus
//                        // Pause playback immediately
//                        //mediaController.getTransportControls().pause();
//                        if (mediaPlayer != null) {
//                            if (mediaPlayer!!.isPlaying) {
//                                mediaPlayer!!.pause()
//                            }
//                        }
//                        // Wait 30 seconds before stopping playback
//                        handler!!.postDelayed(
//                            delayedStopRunnable,
//                            TimeUnit.SECONDS.toMillis(30)
//                        )
//                    } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
//                        // Pause playback
//                    } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
//                        // Lower the volume, keep playing
//                    } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
//                        // Your app has been granted audio focus again
//                        // Raise volume to normal, restart playback if necessary
//                    }
//                }
//                val keyguardManager =
//                    getSystemService<Any>(Context.KEYGUARD_SERVICE) as KeyguardManager
//                mediaPlayer = MediaPlayer.create(this, Settings.System.DEFAULT_RINGTONE_URI)
//                mediaPlayer!!.isLooping = true
//                //mediaPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    handler = Handler()
//                    playbackAttributes = AudioAttributes.Builder()
//                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
//                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
//                        .build()
//                    val focusRequest =
//                        AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
//                            .setAudioAttributes(playbackAttributes)
//                            .setAcceptsDelayedFocusGain(true)
//                            .setOnAudioFocusChangeListener(afChangeListener, handler)
//                            .build()
//                    val res = audioManager!!.requestAudioFocus(focusRequest)
//                    if (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
//                        if (!keyguardManager.isDeviceLocked) {
//                            mediaPlayer!!.start()
//                        }
//                    }
//                } else {
//
//                    // Request audio focus for playback
//                    val result = audioManager!!.requestAudioFocus(
//                        afChangeListener,  // Use the music stream.
//                        AudioManager.STREAM_MUSIC,  // Request permanent focus.
//                        AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
//                    )
//                    if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
//                        if (!keyguardManager.isDeviceLocked) {
//                            // Start playback
//                            mediaPlayer!!.start()
//                        }
//                    }
//                }
//            } else if (vstatus) {
//                mvibrator = getSystemService<Any>(Context.VIBRATOR_SERVICE) as Vibrator
//                // Start without a delay
//                // Each element then alternates between vibrate, sleep, vibrate, sleep...
//                val pattern = longArrayOf(
//                    0, 250, 200, 250, 150, 150, 75,
//                    150, 75, 150
//                )
//
//                // The '-1' here means to vibrate once, as '-1' is out of bounds in the pattern array
//                mvibrator!!.vibrate(pattern, 0)
//                Log.e("Service!!", "vibrate mode start")
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//
//        if (intent != null && intent.extras != null) {
//            data = intent.extras
//            name = data!!.getString("inititator")
//            callType = if (AppController.getInstance().getCall_type()
//                    .equalsIgnoreCase(ApplicationRef.Constants.AUDIO_CALL)
//            ) {
//                "Audio"
//            } else {
//                "Video"
//            }
//        }
//        try {
//            val receiveCallAction = Intent(
//                this,
//                CallReceiver::class.java
//            )
//            receiveCallAction.putExtra(
//                "ConstantApp.CALL_RESPONSE_ACTION_KEY",
//                "ConstantApp.CALL_RECEIVE_ACTION"
//            )
//            receiveCallAction.putExtra("ACTION_TYPE", "RECEIVE_CALL")
//            receiveCallAction.putExtra("NOTIFICATION_ID", NOTIFICATION_ID)
//            receiveCallAction.action = "RECEIVE_CALL"
//            val cancelCallAction = Intent(
//                this,
//                CallReceiver::class.java
//            )
//            cancelCallAction.putExtra(
//                "ConstantApp.CALL_RESPONSE_ACTION_KEY",
//                "ConstantApp.CALL_CANCEL_ACTION"
//            )
//            cancelCallAction.putExtra("ACTION_TYPE", "CANCEL_CALL")
//            cancelCallAction.putExtra("NOTIFICATION_ID", NOTIFICATION_ID)
//            cancelCallAction.action = "CANCEL_CALL"
//            val callDialogAction = Intent(
//                this,
//                CallReceiver::class.java
//            )
//            callDialogAction.putExtra("ACTION_TYPE", "DIALOG_CALL")
//            callDialogAction.putExtra("NOTIFICATION_ID", NOTIFICATION_ID)
//            callDialogAction.action = "DIALOG_CALL"
//            val receiveCallPendingIntent = PendingIntent.getBroadcast(
//                this,
//                1200,
//                receiveCallAction,
//                PendingIntent.FLAG_UPDATE_CURRENT
//            )
//            val cancelCallPendingIntent = PendingIntent.getBroadcast(
//                this,
//                1201,
//                cancelCallAction,
//                PendingIntent.FLAG_UPDATE_CURRENT
//            )
//            val callDialogPendingIntent = PendingIntent.getBroadcast(
//                this,
//                1202,
//                callDialogAction,
//                PendingIntent.FLAG_UPDATE_CURRENT
//            )
//            createChannel()
//            var notificationBuilder: NotificationCompat.Builder? = null
//            if (data != null) {
//                // Uri ringUri= Settings.System.DEFAULT_RINGTONE_URI;
//                notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
//                    .setContentTitle(name)
//                    .setContentText("Incoming $callType Call")
//                    .setSmallIcon(R.drawable.ic_call_icon)
//                    .setPriority(NotificationCompat.PRIORITY_MAX)
//                    .setCategory(NotificationCompat.CATEGORY_CALL)
//                    .addAction(
//                        R.drawable.ic_call_decline,
//                        getString(R.string.reject_call),
//                        cancelCallPendingIntent
//                    )
//                    .addAction(
//                        R.drawable.ic_call_accept,
//                        getString(R.string.answer_call),
//                        receiveCallPendingIntent
//                    )
//                    .setAutoCancel(true) //.setSound(ringUri)
//                    .setFullScreenIntent(callDialogPendingIntent, true)
//            }
//            var incomingCallNotification: Notification? = null
//            if (notificationBuilder != null) {
//                incomingCallNotification = notificationBuilder.build()
//            }
//            startForeground(NOTIFICATION_ID, incomingCallNotification)
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//
//        return START_STICKY
//    }
//
//    override fun onDestroy() {
//        super.onDestroy() // release your media player here audioManager.abandonAudioFocus(afChangeListener);
//        releaseMediaPlayer()
//        releaseVibration()
//    }
//
//    fun createChannel() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            try {
//                val ringUri: Uri = Settings.System.DEFAULT_RINGTONE_URI
//                val channel = NotificationChannel(
//                    CHANNEL_ID,
//                    CHANNEL_NAME,
//                    NotificationManager.IMPORTANCE_HIGH
//                )
//                channel.description = "Call Notifications"
//                channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
//                /* channel.setSound(ringUri,
//                    new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
//                            .setLegacyStreamType(AudioManager.STREAM_RING)
//                            .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION).build());*/Objects.requireNonNull(
//                    getSystemService(NotificationManager::class.java)
//                ).createNotificationChannel(channel)
//            } catch (e: java.lang.Exception) {
//                e.printStackTrace()
//            }
//        }
//    }
//
//    fun releaseVibration() {
//        try {
//            if (mvibrator != null) {
//                if (mvibrator!!.hasVibrator()) {
//                    mvibrator!!.cancel()
//                }
//                mvibrator = null
//            }
//        } catch (e: java.lang.Exception) {
//            e.printStackTrace()
//        }
//    }
//
//    private fun releaseMediaPlayer() {
//        try {
//            if (mediaPlayer != null) {
//                if (mediaPlayer!!.isPlaying) {
//                    mediaPlayer!!.stop()
//                    mediaPlayer!!.reset()
//                    mediaPlayer!!.release()
//                }
//                mediaPlayer = null
//            }
//        } catch (e: java.lang.Exception) {
//            e.printStackTrace()
//        }
//    }
//
//    fun onPrepared(mediaPlayer: MediaPlayer?) {}
}