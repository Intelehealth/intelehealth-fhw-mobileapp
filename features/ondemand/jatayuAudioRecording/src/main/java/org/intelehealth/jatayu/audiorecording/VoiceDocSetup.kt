package org.intelehealth.jatayu.audiorecording

import android.content.Context
import androidx.annotation.DrawableRes
import com.jatayu.voicedoc.sdk.NotificationConfig
import com.jatayu.voicedoc.sdk.TokenProvider
import com.jatayu.voicedoc.sdk.VoiceDoc
import com.jatayu.voicedoc.sdk.VoiceDocConfig

internal object VoiceDocSetup {

    /**
     * Switches the VoiceDoc SDK on. Called once at app start via [VisitVoiceRecorder.init].
     *
     * @param smallIcon icon for the "recording" notification shown while capture runs in the background
     */
    fun initialize(context: Context, @DrawableRes smallIcon: Int, tokenProvider: TokenProvider) {
        if (VoiceDoc.isInitialized) return
        VoiceDoc.initialize(
            context.applicationContext,
            VoiceDocConfig(
                coreApiUrl = BuildConfig.VOICEDOC_CORE_URL,
                notification = NotificationConfig(smallIcon = smallIcon),
            ),
            tokenProvider,
        )
    }
}
