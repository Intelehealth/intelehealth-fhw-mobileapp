package org.intelehealth.core.network.service

import com.codeglo.coyamore.data.PreferenceHelper
import com.codeglo.coyamore.data.PreferenceHelper.Companion.AUTH_TOKEN
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException


class AuthInterceptor(private val preferenceHelper: PreferenceHelper) : Interceptor {
    private val token: String? = preferenceHelper.get(AUTH_TOKEN)

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val authToken: String? = preferenceHelper.get(AUTH_TOKEN)
        return authToken?.let {
            val original: Request = chain.request()
            val builder: Request.Builder = original.newBuilder()
                .header("Authorization", "Bearer $authToken")
            val request: Request = builder.build()
            return@let chain.proceed(request)
        } ?: chain.proceed(chain.request())
    }

    fun hasToken() = !token.isNullOrEmpty()
}
