package org.intelehealth.app.networkApiCalls.interceptors

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.intelehealth.app.R
import org.intelehealth.app.app.IntelehealthApplication
import org.intelehealth.app.utilities.SessionManager
import java.io.IOException

/**
 * Created by Tanvir Hasan on 15-02-2024 : 15-49.
 * Email: mhasan@intelehealth.org
 *
 * Created the interceptor to add token on header to our request
 */
class TokenSetupInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val sessionManager =
            SessionManager(IntelehealthApplication.getAppContext())
        val request = chain.request()
        //Not going to add token on some api
        //that's why added the logic
        //all appointment api's here
        //add key keyword from url to add token to specific url
        if (chain.request().url.encodedPath.contains("cancelAppointment") ||
            chain.request().url.encodedPath.contains("getAppointmentSlots") ||
            chain.request().url.encodedPath.contains("getSlots")
        ) {
            val token = sessionManager.jwtAuthToken

            //adding token here
            val builder: Request.Builder = request.newBuilder()
            builder.header("Authorization", "Bearer $token")
            builder.method(request.method, request.body)
            val response = chain.proceed(builder.build())

            //if response code 401 the exception will throw
            if (response.code == 401) {
                throw LogoutException()
            }
            return response
        } else {
            return chain.proceed(request)
        }


    }
}

/**
 * New exception to detect logout
 */

class LogoutException : IOException() {
    override val message: String
        get() = IntelehealthApplication.getInstance().getString(R.string.token_expired)
}