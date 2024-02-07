package org.intelehealth.videolibrary.utils

import retrofit2.Response

class ResponseChecker<T>(private val response: Response<T>) {
    val isNotAuthorized: Boolean
        get() = response.code() == 401
}