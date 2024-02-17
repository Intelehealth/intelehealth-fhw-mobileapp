package org.intelehealth.app.networkApiCalls.interceptors

import okhttp3.Interceptor
import okhttp3.Response
import org.intelehealth.app.app.IntelehealthApplication
import org.intelehealth.app.utilities.SessionManager

/**
 * Created by Tanvir Hasan on 15-02-2024 : 15-49.
 * Email: mhasan@intelehealth.org
 *
 * Created the interceptor to add token on header to our request
 */
class TokenSetupInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val sessionManager = SessionManager(IntelehealthApplication.getAppContext())
        val token = sessionManager.token
        var modifiedRequest = chain.request()
        //Not going to add token on some api with 3004 port
        //that's why added the logic
        if (chain.request().url.port == 3004) {
            modifiedRequest = chain.request()
                .newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        }
        return chain.proceed(modifiedRequest)
    }
}