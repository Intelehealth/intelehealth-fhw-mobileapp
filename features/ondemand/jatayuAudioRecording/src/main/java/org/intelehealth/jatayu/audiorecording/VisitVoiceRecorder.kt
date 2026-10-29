package org.intelehealth.jatayu.audiorecording

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.github.ajalt.timberkt.Timber
import com.jatayu.voicedoc.sdk.FinalizeResult
import com.jatayu.voicedoc.sdk.SessionParams
import com.jatayu.voicedoc.sdk.VoiceDoc
import com.jatayu.voicedoc.sdk.VoiceDocEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.intelehealth.jatayu.audiorecording.auth.VoiceDocTokenProvider

/**
 * Records the consultation for one visit through the VoiceDoc SDK: started when a new visit
 * begins, stopped once the visit has been created.
 *
 * Calls return immediately and run in an app-wide scope, because stop() keeps working
 * (finalising chunks, fetching the summary) after the visit screen has finished. The lock keeps
 * init/start/stop/cancel in call order, so a quick stop can't overtake a start that is still
 * signing in.
 *
 * Each VoiceDoc session is named "<visitUuid>_<startMillis>", so a result arriving later (a
 * recording finished offline, or after an app restart) can still be matched back to its visit,
 * and a visit can be recorded more than once.
 */
object VisitVoiceRecorder {

    /**
     * Appendix A (I02 doc) sections VoiceDoc should fill from the conversation. metadata,
     * doctorInfo and patientInfo come from NAS itself, so they aren't requested.
     */
    private val SUMMARY_FIELDS = listOf(
        "patientPresentation",
        "medications",
        "systemicReview",
        "specializedHistory",
        "physicalExamination",
    )

    private const val NAME_SEPARATOR = '_'

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val lock = Mutex()
    private val tokenProvider = VoiceDocTokenProvider()

    @Volatile
    private var activeVisitUuid: String? = null

    /** Set when the SDK reports SessionExpired: a fresh sign-in was already tried and rejected. */
    @Volatile
    private var signInRejected = false

    /** True once [init] has run, i.e. VoiceDoc recording is switched on for this build. */
    @JvmStatic
    fun isEnabled(): Boolean = VoiceDoc.isInitialized

    /** Runtime permissions to request before [startForVisit]. */
    @JvmStatic
    fun requiredPermissions(): Array<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.POST_NOTIFICATIONS)
        } else {
            arrayOf(Manifest.permission.RECORD_AUDIO)
        }

    /**
     * Switches the SDK on. Call once from Application.onCreate, so chunks left over from an
     * earlier run are retried right away and background results are never missed.
     *
     * @param smallIcon icon for the ongoing-recording notification
     */
    @JvmStatic
    fun init(context: Context, @DrawableRes smallIcon: Int) {
        if (VoiceDoc.isInitialized) return
        VoiceDocSetup.initialize(context, smallIcon, tokenProvider)
        scope.launch {
            VoiceDoc.events.collect { event ->
                when (event) {
                    is VoiceDocEvent.SessionCompleted -> onResult(event.result)
                    is VoiceDocEvent.SessionExpired -> {
                        // The SDK already called refreshAccessToken() and that token was rejected
                        // too, so retrying now would only repeat the 401. Retried after the next
                        // successful sign-in (see retryAfterSignIn).
                        signInRejected = true
                        Timber.e { "VoiceDoc: session expired, refreshed token was rejected too" }
                    }
                    is VoiceDocEvent.CaptureProblem ->
                        Timber.w { "VoiceDoc: capture problem in ${event.sessionId}: ${event.message}" }
                    else -> Unit
                }
            }
        }
        scope.launch { lock.withLock { finishInterruptedSessions() } }
    }

    /**
     * Starts recording for [visitUuid]. The SDK signs in through [VoiceDocTokenProvider] as part
     * of this. Does nothing when [init] hasn't run (VoiceDoc isn't enabled for this build).
     *
     * @param language app language code, e.g. "en" or "hi"
     */
    @JvmStatic
    fun startForVisit(context: Context, visitUuid: String, language: String) {
        if (!VoiceDoc.isInitialized) return
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Timber.w { "VoiceDoc: no RECORD_AUDIO permission, visit $visitUuid not recorded" }
            return
        }
        scope.launch {
            lock.withLock {
                if (signInRejected) retryAfterSignIn()
                runCatching {
                    val sessionId = VoiceDoc.startSession(
                        SessionParams(
                            ownerEmail = BuildConfig.VOICEDOC_EMAIL,
                            language = language,
                            fields = SUMMARY_FIELDS,
                            name = "$visitUuid$NAME_SEPARATOR${System.currentTimeMillis()}",
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
                    .onSuccess(::onResult)
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

    /** Logs out of VoiceDoc; call when the NAS user logs out. Fire-and-forget. */
    @JvmStatic
    fun logout() {
        if (!VoiceDoc.isInitialized) return
        scope.launch { lock.withLock { tokenProvider.logout() } }
    }

    /** Signs in again and, only if that succeeds, lets the SDK retry the chunks stuck on 401. */
    private suspend fun retryAfterSignIn() {
        if (tokenProvider.refreshAccessToken() == null) return
        signInRejected = false
        VoiceDoc.retryNow()
    }

    /**
     * Recordings cut short by an app kill or crash survive in the SDK but can't be stopped
     * through [stopForVisit] any more. Finish them so their summary still reaches the visit.
     */
    private suspend fun finishInterruptedSessions() {
        val interrupted = runCatching { VoiceDoc.interruptedSessions() }
            .onFailure { Timber.e(it) { "VoiceDoc: could not list interrupted sessions" } }
            .getOrDefault(emptyList())
        for (session in interrupted) {
            Timber.d { "VoiceDoc: finishing interrupted session ${session.sessionId}" }
            runCatching { VoiceDoc.finishInterruptedSession(session.sessionId) }
                .onSuccess(::onResult)
                .onFailure { Timber.w(it) { "VoiceDoc: interrupted session ${session.sessionId} not finished yet" } }
        }
    }

    private fun onResult(result: FinalizeResult) {
        val visitUuid = result.name?.substringBefore(NAME_SEPARATOR)
        // TODO: map the summary (Appendix A of the I02 doc) onto the visit.
        Timber.d {
            "VoiceDoc: result for visit $visitUuid (session ${result.sessionId}), " +
                "transcript ${result.transcript?.length ?: 0} chars, summary keys ${result.summary?.keys}"
        }
        //print summary for debugging
        Timber.d { "VoiceDoc: summary for visit $visitUuid: ${result.summary}" }
    }
}
