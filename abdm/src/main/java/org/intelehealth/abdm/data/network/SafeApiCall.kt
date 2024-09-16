package org.intelehealth.abdm.data.network

import android.util.Log
import org.intelehealth.abdm.domain.result.ApiResult
import retrofit2.Response

object SafeApiCall {
    suspend fun <T : Any, R : Any> call(
        apiCall: suspend () -> Response<T>,
        mapper: (T) -> R
    ): ApiResult<R> {
        return try {
            val response = apiCall.invoke()
            if (response.code() ==200) {
                val body = response.body()
                Log.e("RESPONSE SUCCESS",body.toString())
                if (body != null) {
                    ApiResult.Success(mapper(body))
                } else {
                    ApiResult.Error(Throwable("Response body is null"),200)
                }
            } else {
                Log.e("RESPONSE SUCCESS",response.toString())
                ApiResult.Error(Throwable(ErrorFactory.getErrorMessageFromCode(response.code())),response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error(Throwable(ErrorFactory.getErrorMessage(e)),0)
        }
    }
}