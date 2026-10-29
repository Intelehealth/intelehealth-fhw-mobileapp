package org.intelehealth.jatayu.audiorecording

import android.content.Context
import androidx.annotation.DrawableRes
import com.jatayu.voicedoc.sdk.NotificationConfig
import com.jatayu.voicedoc.sdk.VoiceDoc
import com.jatayu.voicedoc.sdk.VoiceDocConfig
import org.intelehealth.jatayu.audiorecording.auth.VoiceDocTokenProvider

object VoiceDocSetup {

    /**
     * Switches the VoiceDoc SDK on. Call once from Application.onCreate.
     *
     * @param smallIcon icon for the "recording" notification shown while capture runs in the background
     */
    fun initialize(context: Context, @DrawableRes smallIcon: Int) {
        if (VoiceDoc.isInitialized) return
        VoiceDoc.initialize(
            context.applicationContext,
            VoiceDocConfig(
                coreApiUrl = BuildConfig.VOICEDOC_CORE_URL,
                notification = NotificationConfig(smallIcon = smallIcon),
            ),
            VoiceDocTokenProvider(),
        )
    }
}
