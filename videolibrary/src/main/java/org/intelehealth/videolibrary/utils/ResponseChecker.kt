package org.intelehealth.videolibrary.utils

import retrofit2.Response

/**
 * Created by Arpan Sircar. on 08-02-2024.
 * Email : arpan@intelehealth.org
 * Mob   : +919123116015
 **/

class ResponseChecker<T>(private val response: Response<T>) {
    val isNotAuthorized: Boolean
        get() = response.code() == 401
}