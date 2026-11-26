package org.intelehealth.jatayu.audiorecording.api

import com.github.ajalt.timberkt.Timber
import org.intelehealth.jatayu.audiorecording.auth.VoiceDocTokenProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.intelehealth.jatayu.audiorecording.BuildConfig
import org.json.JSONObject
import org.json.JSONTokener
import java.io.IOException
import java.net.HttpURLConnection
import java.util.concurrent.TimeUnit

/**
 * POST /api/patient-visit-summary on the VoiceDoc core API: the patient summary for a finished
 * recording, looked up by account email and the recording's session name.
 */
internal class PatientVisitSummaryApi(
    private val tokenProvider: VoiceDocTokenProvider,
    private val coreBaseUrl: String = BuildConfig.VOICEDOC_CORE_URL,
    private val client: OkHttpClient = OkHttpClient.Builder()
        .callTimeout(60, TimeUnit.SECONDS)
        .build(),
) {

    /** Returns the summary as JSON text. Retries once with a refreshed token on 401. */
    suspend fun fetch(sessionName: String): String {
        val token = tokenProvider.getAccessToken() ?: throw IOException("VoiceDoc sign-in failed")
        val first = post(sessionName, token)
        if (first.code != HttpURLConnection.HTTP_UNAUTHORIZED) return first.bodyOrThrow()
        val refreshed = tokenProvider.refreshAccessToken() ?: throw IOException("VoiceDoc sign-in failed")
        return post(sessionName, refreshed).bodyOrThrow()
    }

    private suspend fun post(sessionName: String, token: String): Response = withContext(Dispatchers.IO) {
        val email = tokenProvider.email
        val body = JSONObject().put("email", email).put("name", sessionName).toString()
        // log email and session name, but not the body (which contains the email again)
        Timber.d { "VoiceDoc: POST /api/patient-visit-summary for email=$email session=$sessionName" }
        val request = Request.Builder()
            .url("$coreBaseUrl/api/patient-visit-summary")
            .header("Authorization", "Bearer $token")
            .header("accept", "*/*")
            .post(body.toRequestBody("application/json".toMediaType()))
            .build()
        Timber.d { "VoiceDoc: POST /api/patient-visit-summary body: $body" }
        Timber.d { "VoiceDoc: POST /api/patient-visit-summary request: $request" }
        Timber.d { "VoiceDoc: POST /api/patient-visit-summary request headers: ${request.headers}" }
        client.newCall(request).execute().use { Response(it.code, it.body?.string().orEmpty()) }
    }

    private class Response(val code: Int, val body: String) {
        fun bodyOrThrow(): String {
            if (code !in 200..299) throw IOException("patient-visit-summary failed: HTTP $code, body: $body")
            val summary = unwrap(body)
            // An empty answer means the server hasn't produced the summary yet; the caller retries.
            if (summary.isBlank() || summary.trim() in setOf("{}", "[]", "null", "\"\"")) {
                throw IOException("patient-visit-summary not ready yet (HTTP $code, empty body)")
            }
            // Until the recording is processed the server answers 200 with
            // {"overall_summary":"No visit records found ...","visits_found":0,"visits":[]}.
            val visitsFound = runCatching { JSONObject(summary).optInt("visits_found", -1) }.getOrDefault(-1)
            if (visitsFound == 0) throw IOException("patient-visit-summary not ready yet (visits_found=0)")
            return summary
        }
    }

    private companion object {
        /** VoiceDoc sometimes returns JSON wrapped in a JSON string (as login does); unwrap it. */
        fun unwrap(raw: String): String =
            runCatching { (JSONTokener(raw).nextValue() as? String)?.takeIf { it.trimStart().startsWith("{") || it.trimStart().startsWith("[") } }
                .getOrNull() ?: raw
    }
}
