// LocalBroadcastManager is deprecated but still works, and it's what NAS uses for in-app events.
@file:Suppress("DEPRECATION")

package org.intelehealth.jatayu.audiorecording

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.github.ajalt.timberkt.Timber
import com.jatayu.voicedoc.sdk.FinalizeResult
import com.jatayu.voicedoc.sdk.SessionParams
import com.jatayu.voicedoc.sdk.VoiceDoc
import com.jatayu.voicedoc.sdk.VoiceDocEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.intelehealth.jatayu.audiorecording.api.NetworkWaiter
import org.intelehealth.jatayu.audiorecording.api.PatientVisitSummaryApi
import org.intelehealth.jatayu.audiorecording.auth.VoiceDocTokenProvider
import org.json.JSONObject
import org.json.JSONTokener

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
 *
 * Results are announced in-app through LocalBroadcastManager:
 *  1. [ACTION_RESULT_CAPTURED] as soon as VoiceDoc returns the transcript and section summary;
 *  2. [ACTION_PATIENT_SUMMARY_CAPTURED] right after, with the patient visit summary
 *     (FinalizeResult.shortSummary), when VoiceDoc returned one.
 */
object VisitVoiceRecorder {

    const val ACTION_RESULT_CAPTURED = "org.intelehealth.jatayu.audiorecording.RESULT_CAPTURED"
    const val ACTION_PATIENT_SUMMARY_CAPTURED = "org.intelehealth.jatayu.audiorecording.PATIENT_SUMMARY_CAPTURED"

    /** NAS visit UUID the recording belongs to. Both actions. */
    const val EXTRA_VISIT_UUID = "visitUuid"
    /** VoiceDoc session name ("<visitUuid>_<startMillis>"). Both actions. */
    const val EXTRA_SESSION_NAME = "sessionName"
    const val EXTRA_SESSION_ID = "sessionId"
    const val EXTRA_TRANSCRIPT = "transcript"
    /** Section summary as a JSON object string, e.g. {"Vitals":[...],"Family History":[...]}. */
    const val EXTRA_SUMMARY_JSON = "summaryJson"
    /** Same as [EXTRA_SUMMARY_JSON] in the consultation's original language. */
    const val EXTRA_ORIGINAL_LANG_SUMMARY_JSON = "originalLangSummaryJson"
    /** Patient visit summary (FinalizeResult.shortSummary). */
    const val EXTRA_PATIENT_SUMMARY_JSON = "patientSummaryJson"

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

    /**
     * Waits between /api/patient-visit-summary attempts: the patient summary may not be ready
     * the moment the VoiceDoc result arrives. 7 online attempts over ~6 minutes, then give up.
     * Time spent offline doesn't count: the retry waits for internet first.
     */
    private val PATIENT_SUMMARY_RETRY_DELAYS_MS = longArrayOf(10_000, 20_000, 40_000, 60_000, 120_000, 120_000)

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val lock = Mutex()
    private val tokenProvider = VoiceDocTokenProvider()
    private val patientSummaryApi = PatientVisitSummaryApi(tokenProvider)
    private lateinit var appContext: Context
    private val networkWaiter by lazy { NetworkWaiter(appContext) }

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
     * @param providerId logged-in provider, if any; their VoiceDoc account is providerId + suffix
     */
    @JvmStatic
    fun init(context: Context, @DrawableRes smallIcon: Int, providerId: String?) {
        if (VoiceDoc.isInitialized) return
        appContext = context.applicationContext
        tokenProvider.setProviderId(providerId)
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
    fun startForVisit(context: Context, providerId: String, visitUuid: String, language: String) {
        if (!VoiceDoc.isInitialized) return
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Timber.w { "VoiceDoc: no RECORD_AUDIO permission, visit $visitUuid not recorded" }
            return
        }
        scope.launch {
            lock.withLock {
                tokenProvider.setProviderId(providerId)
                if (signInRejected) retryAfterSignIn()
                runCatching {
                    val sessionId = VoiceDoc.startSession(
                        SessionParams(
                            ownerEmail = tokenProvider.email,
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

    /** Announces the VoiceDoc result, then fetches the patient summary for the same recording. */
    private fun onResult(result: FinalizeResult) {
        val sessionName = result.name
        val visitUuid = sessionName.substringBefore(NAME_SEPARATOR)
        // The SDK now hands the summary over as JSON text as well; fall back to the map.
        val summaryJson = result.summaryJson?.takeIf { it.isNotBlank() } ?: toJson(result.summary).toString()
        val shortSummary = result.shortSummary

        Timber.d {
            "VoiceDoc: result for visit $visitUuid (session ${result.sessionId}), " +
                "transcript ${result.transcript.length} chars, summary $summaryJson, shortSummary $shortSummary"
        }
        broadcast(
            Intent(ACTION_RESULT_CAPTURED)
                .putExtra(EXTRA_VISIT_UUID, visitUuid)
                .putExtra(EXTRA_SESSION_NAME, sessionName)
                .putExtra(EXTRA_SESSION_ID, result.sessionId)
                .putExtra(EXTRA_TRANSCRIPT, result.transcript)
                .putExtra(EXTRA_SUMMARY_JSON, summaryJson)
                .putExtra(EXTRA_ORIGINAL_LANG_SUMMARY_JSON, toJson(result.originalLangSummary).toString())
        )

        // The patient visit summary now comes inside the result, so it's announced straight away
        // (receivers save it after the result above, in order).
        if (shortSummary.isNullOrBlank()) {
            Timber.w { "VoiceDoc: result for visit $visitUuid has no patient visit summary (shortSummary)" }
        } else {
            broadcast(
                Intent(ACTION_PATIENT_SUMMARY_CAPTURED)
                    .putExtra(EXTRA_VISIT_UUID, visitUuid)
                    .putExtra(EXTRA_SESSION_NAME, sessionName)
                    .putExtra(EXTRA_PATIENT_SUMMARY_JSON, shortSummary)
            )
        }
        // Earlier flow: patient summary fetched separately from /api/patient-visit-summary.
       /* scope.launch {
            val patientSummary = fetchPatientSummaryWithRetry(visitUuid, sessionName) ?: return@launch
            Timber.d { "VoiceDoc: patient summary for visit $visitUuid: $patientSummary" }
            broadcast(
                Intent(ACTION_PATIENT_SUMMARY_CAPTURED)
                    .putExtra(EXTRA_VISIT_UUID, visitUuid)
                    .putExtra(EXTRA_SESSION_NAME, sessionName)
                    .putExtra(EXTRA_PATIENT_SUMMARY_JSON, patientSummary)
            )
        }*/
    }

    /**
     * Calls /api/patient-visit-summary until it returns a summary, waiting longer each time.
     * Only attempts made with internet count towards the limit; while offline it waits for the
     * connection to come back (as long as the app process is alive).
     */
    private suspend fun fetchPatientSummaryWithRetry(visitUuid: String?, sessionName: String): String? {
        val attempts = PATIENT_SUMMARY_RETRY_DELAYS_MS.size + 1
        var attempt = 0
        while (true) {
            if (!networkWaiter.isOnline()) {
                Timber.w { "VoiceDoc: offline, patient summary for visit $visitUuid waits for internet" }
                networkWaiter.awaitOnline()
            }
            Timber.d { "VoiceDoc: fetching patient summary for visit $visitUuid (attempt ${attempt + 1}/$attempts)" }
            val result = runCatching { patientSummaryApi.fetch(sessionName) }
            result.getOrNull()?.let { return it }
            // Connection dropped during the call: not the server's answer, so don't count it.
            if (!networkWaiter.isOnline()) continue

            attempt++
            if (attempt == attempts) {
                Timber.e(result.exceptionOrNull()) { "VoiceDoc: patient summary for visit $visitUuid failed after $attempts attempts" }
                return null
            }
            val wait = PATIENT_SUMMARY_RETRY_DELAYS_MS[attempt - 1]
            Timber.w { "VoiceDoc: patient summary for visit $visitUuid not available (attempt $attempt/$attempts): ${result.exceptionOrNull()?.message}; retrying in ${wait / 1000}s" }
            delay(wait)
        }
    }

    private fun broadcast(intent: Intent) {
        LocalBroadcastManager.getInstance(appContext).sendBroadcast(intent)
    }

    /** The SDK returns each summary section as JSON text; nest it as real JSON. */
    private fun toJson(sections: Map<String, Any?>?): JSONObject = JSONObject().apply {
        sections?.forEach { (section, value) ->
            put(section, when (value) {
                is String -> runCatching { JSONTokener(value).nextValue() }.getOrDefault(value)
                else -> JSONObject.wrap(value) ?: JSONObject.NULL
            })
        }
    }
}
