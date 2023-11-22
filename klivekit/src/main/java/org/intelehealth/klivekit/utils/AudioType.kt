package org.intelehealth.klivekit.utils

import com.twilio.audioswitch.AudioDevice

/**
 * Created by Vaghela Mithun R. on 19-07-2023 - 16:20.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
enum class AudioType(val value: String) {
    BLUETOOTH_HEADSET("Bluetooth"),
    WIRED_HEADSET("Wired Headset"),
    EARPIECE("Earpiece"),
    SPEAKER_PHONE("Speakerphone");
}