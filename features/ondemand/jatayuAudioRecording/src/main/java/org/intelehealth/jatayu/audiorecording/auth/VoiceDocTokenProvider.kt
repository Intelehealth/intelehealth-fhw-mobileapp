package org.intelehealth.jatayu.audiorecording.auth

import android.util.Base64
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

    /**
     * Called by the SDK after the backend rejected the current token. Returns a freshly
     * signed-in token, or null if sign-in fails (the SDK then emits SessionExpired).
     */
    override suspend fun refreshAccessToken(): String? {
        val stale = token
        return mutex.withLock {
            // Another caller may have refreshed while we waited for the lock.
            token?.takeIf { it != stale } ?: runCatching { login() }
                .onFailure { Timber.e(it) { "VoiceDoc: refresh sign-in failed" } }
                .getOrNull()
        }
    }

    /**
     * Ends the VoiceDoc session on the auth service (POST /logout, I02 doc) and drops the cached
     * token. Does nothing if we never signed in. The next SDK call signs in again, so recordings
     * still waiting to upload carry on afterwards.
     */
    suspend fun logout() {
        val current = mutex.withLock { token.also { token = null } } ?: return
        withContext(Dispatchers.IO) {
            val body = JSONObject().put("emailid", email).toString()
            val request = Request.Builder()
                .url("$authBaseUrl/logout")
                .header("Authorization", "Bearer $current")
                .post(body.toRequestBody("application/json".toMediaType()))
                .build()
            runCatching {
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) Timber.d { "VoiceDoc: logged out $email" }
                    else Timber.w { "VoiceDoc logout failed: HTTP ${response.code}, body: ${response.body?.string()}" }
                }
            }.onFailure { Timber.w(it) { "VoiceDoc logout request failed" } }
        }
    }

    private suspend fun login(): String = withContext(Dispatchers.IO) {
        check(email.isNotBlank() && password.isNotBlank()) {
            "VoiceDoc credentials missing: set jatayu.voicedoc.email/password in local.properties"
        }
        val body = JSONObject().put("email", email).put("pkd", password).toString()
        Timber.d { "VoiceDoc: signing in as $email at $authBaseUrl" }
        val request = Request.Builder()
            .url("$authBaseUrl/api/auth/login_wrapper")
            .post(body.toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            val raw = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                // The server returns a JSON error message on failure; it contains no token.
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
        val token = json.optString("token").trim().takeIf { it.isNotBlank() }
            ?: throw IOException("VoiceDoc login response has no token (keys: ${json.keys().asSequence().toList()})")
        if (BuildConfig.DEBUG) logTokenClaims(json, token)
        return token
    }

    /**
     * Debug aid for 401s from the core API: shows which kind of token sign-in returned and for
     * which project/account, without logging the token itself. A Firebase ID token has
     * iss=https://securetoken.google.com/<project>; a custom token has the identitytoolkit aud
     * and must be exchanged before the core API accepts it.
     */
    private fun logTokenClaims(response: JSONObject, token: String) {
        runCatching {
            val payload = token.split('.')[1]
            val claims = JSONObject(String(Base64.decode(payload, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)))
            Timber.d {
                "VoiceDoc: login response keys=${response.keys().asSequence().toList()}, token claims " +
                    "iss=${claims.optString("iss")} aud=${claims.optString("aud")} " +
                    "email=${claims.optString("email")} exp=${claims.optLong("exp")} " +
                    "sameAsRefreshToken=${token == response.optString("refreshtoken")}"
            }
        }.onFailure { Timber.w { "VoiceDoc: token is not a JWT (length ${token.length})" } }
    }
}
