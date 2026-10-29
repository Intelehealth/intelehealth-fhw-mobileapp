package org.intelehealth.jatayu.audiorecording.auth

import com.github.ajalt.timberkt.Timber
import com.jatayu.voicedoc.sdk.TokenProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.intelehealth.jatayu.audiorecording.BuildConfig
import org.json.JSONObject
import org.json.JSONTokener
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Supplies VoiceDoc access tokens to the SDK by signing in with the account Jatayu issued
 * (POST /api/auth/login_wrapper, see I02 doc).
 *
 * Refresh is done by signing in again: the refresh-token endpoint isn't documented, and
 * re-login also sidesteps the rotating refresh token the integration guide warns about.
 */
class VoiceDocTokenProvider(
    private val email: String = BuildConfig.VOICEDOC_EMAIL,
    private val password: String = BuildConfig.VOICEDOC_PASSWORD,
    private val authBaseUrl: String = BuildConfig.VOICEDOC_AUTH_URL,
    private val client: OkHttpClient = OkHttpClient.Builder()
        .callTimeout(30, TimeUnit.SECONDS)
        .build(),
) : TokenProvider {

    private val mutex = Mutex()

    @Volatile
    private var token: String? = null

    override suspend fun getAccessToken(): String =
        token ?: mutex.withLock { token ?: login() }

    override suspend fun refreshAccessToken(): String {
        val stale = token
        return mutex.withLock {
            // Another caller may have refreshed while we waited for the lock.
            token?.takeIf { it != stale } ?: login()
        }
    }

    private suspend fun login(): String = withContext(Dispatchers.IO) {
        check(email.isNotBlank() && password.isNotBlank()) {
            "VoiceDoc credentials missing: set jatayu.voicedoc.email/password in local.properties"
        }
        val body = JSONObject().put("email", email).put("pkd", password).toString()
        Timber.d { "Sending VoiceDoc login request: $body" }
        val request = Request.Builder()
            .url("$authBaseUrl/api/auth/login_wrapper")
            .post(body.toRequestBody("application/json".toMediaType()))
            .build()
        Timber.d { "VoiceDoc login request: $request" }

        client.newCall(request).execute().use { response ->
            val raw = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                // print full response body to logcat, because the server sometimes returns a JSON error message
                Timber.e { "VoiceDoc login failed: HTTP ${response.code}, body: $raw" }
                throw IOException("VoiceDoc login failed: HTTP ${response.code}")
            }
            parseToken(raw).also { token = it }
        }
    }

    /** The response is documented as a JSON-encoded string wrapping {token, refreshtoken}. */
    private fun parseToken(raw: String): String {
        val json = when (val value = JSONTokener(raw).nextValue()) {
            is JSONObject -> value
            is String -> JSONObject(value)
            else -> throw IOException("Unexpected VoiceDoc login response")
        }
        return json.optString("token").takeIf { it.isNotBlank() }
            ?: throw IOException("VoiceDoc login response has no token")
    }
}
