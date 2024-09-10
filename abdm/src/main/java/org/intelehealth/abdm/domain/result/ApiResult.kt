package org.intelehealth.abdm.domain.result

sealed interface ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>
    data class Error(val exception: Throwable) : ApiResult<Nothing>
}