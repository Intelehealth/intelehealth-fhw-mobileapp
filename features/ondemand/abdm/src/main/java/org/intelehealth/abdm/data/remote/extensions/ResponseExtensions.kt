package org.intelehealth.abdm.data.remote.extensions

import org.json.JSONObject
import retrofit2.Response

internal class HttpException(
    val httpCode: Int,
    val errorMessage: String,
    val serverMessage: String? = null,
) : RuntimeException("HTTP $httpCode: $errorMessage")

internal class EmptyResponseException(
    message: String = "Response succeeded but body was empty",
) : RuntimeException(message)

internal class MalformedResponseException(
    message: String,
) : RuntimeException(message)

/** Server returned 200 but rejected the OTP (authResult = "failed"); a wrong OTP, not a transport error. */
internal class OtpVerificationFailedException(
    val serverMessage: String? = null,
) : RuntimeException("OTP verification failed")

/** Returns the body, or throws if the response failed or body is empty. */
internal fun <T> Response<T>.requireBody(): T {
    if (!isSuccessful) throw asHttpException()
    return body() ?: throw EmptyResponseException()
}

internal fun <T> Response<T>.requireSuccess() {
    if (!isSuccessful) throw asHttpException()
}

/** Builds an [HttpException], extracting the server's error-body message when present. */
private fun Response<*>.asHttpException(): HttpException =
    HttpException(code(), message(), parseServerMessage())

/** Best-effort parse of `{"message": …}` from the error body; null on any failure. */
private fun Response<*>.parseServerMessage(): String? = try {
    errorBody()?.string()?.takeIf { it.isNotBlank() }?.let { body ->
        JSONObject(body).optString("message").takeIf { it.isNotBlank() }
    }
} catch (e: Exception) {
    null
}