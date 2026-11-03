package org.intelehealth.abdm.data.remote.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import org.intelehealth.abdm.data.remote.auth.TokenStore
import org.intelehealth.abdm.di.network.BEARER_PREFIX
import org.intelehealth.abdm.di.network.HEADER_AUTHORIZATION
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(private val tokenStore: TokenStore) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        if (request.header(HEADER_AUTHORIZATION) != null) {
            return chain.proceed(request)
        }

        val token = tokenStore.get()
            ?: throw IllegalStateException("Authed endpoint called without a token. Ensure AbdmAuthApi.getToken() succeeded and TokenStore was populated before calling main APIs.")

        val auth = request.newBuilder()
            .addHeader(HEADER_AUTHORIZATION, "$BEARER_PREFIX${token}")
            .build()

        return chain.proceed(auth)
    }
}