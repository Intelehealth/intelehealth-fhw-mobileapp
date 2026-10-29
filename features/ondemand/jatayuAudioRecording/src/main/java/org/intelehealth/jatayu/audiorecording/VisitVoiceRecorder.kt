package org.intelehealth.jatayu.audiorecording

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.github.ajalt.timberkt.Timber
import com.jatayu.voicedoc.sdk.SessionParams
import com.jatayu.voicedoc.sdk.VoiceDoc
import com.jatayu.voicedoc.sdk.VoiceDocEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Records the consultation for one visit through the VoiceDoc SDK: started when a new visit
 * begins, stopped once the visit has been created.
 *
 * Calls return immediately and run in an app-wide scope, because stop() keeps working
 * (finalising chunks, fetching the summary) after the visit screen has finished. The lock keeps
 * start/stop/cancel in call order, so a quick stop can't overtake a start that is still logging in.
 *
 * The VoiceDoc session is named after the visit UUID, so a result arriving later (e.g. a
 * recording finished offline) can still be matched back to its visit.
 */
object VisitVoiceRecorder {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val lock = Mutex()

    @Volatile
    private var activeVisitUuid: String? = null

    /** Runtime permissions to request before [startForVisit]. */
    @JvmStatic
    fun requiredPermissions(): Array<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.POST_NOTIFICATIONS)
        } else {
            arrayOf(Manifest.permission.RECORD_AUDIO)
        }

    /**
     * Initialises the SDK if needed and starts recording for [visitUuid]. The SDK signs in
     * through [org.intelehealth.jatayu.audiorecording.auth.VoiceDocTokenProvider] as part of this.
     *
     * @param language app language code, e.g. "en" or "hi"
     * @param smallIcon icon for the ongoing-recording notification
     */
    @JvmStatic
    fun startForVisit(context: Context, visitUuid: String, language: String, @DrawableRes smallIcon: Int) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Timber.w { "VoiceDoc: no RECORD_AUDIO permission, visit $visitUuid not recorded" }
            return
        }
        val appContext = context.applicationContext
        scope.launch {
            lock.withLock {
                runCatching {
                    initialize(appContext, smallIcon)
                    /*val sessionId = VoiceDoc.startSession(
                        SessionParams(
                            ownerEmail = email,
                            language = "en",
                            specialty = "General OPD",
                            fields = fields,
                            name = "Ramesh K – follow-up 29 Oct",   // optional
                        ),
                    )*/
                    val sessionId = VoiceDoc.startSession(
                        SessionParams(
                            ownerEmail = BuildConfig.VOICEDOC_EMAIL,
                            language = language,
                            name = visitUuid,
                        )
                    )
                    activeVisitUuid = visitUuid
                    Timber.d { "VoiceDoc: recording visit $visitUuid, session $sessionId" }
                }.onFailure { Timber.e(it) { "VoiceDoc: could not start recording for visit $visitUuid" } }
            }
        }
    }

    /** Stops the recording for [visitUuid] and asks VoiceDoc for its transcript and summary. */
    @JvmStatic
    fun stopForVisit(visitUuid: String) {
        scope.launch {
            lock.withLock {
                if (activeVisitUuid != visitUuid) return@withLock
                activeVisitUuid = null
                runCatching { VoiceDoc.stop() }
                    .onSuccess {
                        // log summary json
                        Timber.d { "VoiceDoc: stop for visit $visitUuid succeeded" }
                        Timber.d { "VoiceDoc: summary for visit $visitUuid: ${it.summary}" }
                        onResult(visitUuid, it.transcript, it.summary)
                    }
                    // Offline/incomplete uploads are kept by the SDK and finished later;
                    // that result arrives as a SessionCompleted event instead.
                    .onFailure { Timber.w(it) { "VoiceDoc: stop for visit $visitUuid deferred/failed" } }
            }
        }
    }

    /** Discards the recording for [visitUuid], e.g. when the visit itself is discarded. */
    @JvmStatic
    fun cancelForVisit(visitUuid: String) {
        scope.launch {
            lock.withLock {
                if (activeVisitUuid != visitUuid) return@withLock
                activeVisitUuid = null
                runCatching { VoiceDoc.cancel() }
                    .onFailure { Timber.e(it) { "VoiceDoc: cancel for visit $visitUuid failed" } }
            }
        }
    }

    private fun initialize(context: Context, @DrawableRes smallIcon: Int) {
        if (VoiceDoc.isInitialized) return
        VoiceDocSetup.initialize(context, smallIcon)
        scope.launch {
            VoiceDoc.events.collect { event ->
                when (event) {
                    is VoiceDocEvent.SessionCompleted ->
                        onResult(event.result.name, event.result.transcript, event.result.summary)
                    is VoiceDocEvent.SessionExpired -> {
                        // Our token provider signs in again on refresh, so just retry.
                        Timber.w { "VoiceDoc: session expired, retrying" }
                        VoiceDoc.retryNow()
                    }
                    is VoiceDocEvent.CaptureProblem ->
                        Timber.w { "VoiceDoc: capture problem in ${event.sessionId}: ${event.message}" }
                    else -> Unit
                }
            }
        }
    }

    private fun onResult(visitUuid: String?, transcript: String?, summary: Map<String, String>?) {
        // TODO: map the summary (Appendix A of the I02 doc) onto the visit once the keys are agreed.
        Timber.d { "VoiceDoc: result for visit $visitUuid, transcript ${transcript?.length ?: 0} chars, summary keys ${summary?.keys}" }
    }
}
