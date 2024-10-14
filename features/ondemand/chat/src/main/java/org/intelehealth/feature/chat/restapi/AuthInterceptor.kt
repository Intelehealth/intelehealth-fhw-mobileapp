package org.intelehealth.feature.chat.restapi

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.intelehealth.app.BuildConfig
import org.intelehealth.core.utils.helper.PreferenceHelper
import org.intelehealth.core.utils.helper.PreferenceHelper.Companion.AUTH_TOKEN
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject


class AuthInterceptor @Inject constructor(private val preferenceHelper: PreferenceHelper) :
    Interceptor {
    private val token: String? = preferenceHelper.get(AUTH_TOKEN)

    init {
        Timber.d("############   AUTH TOKEN ---$token")
        println("############   AUTH TOKEN ---$token")
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val authToken: String? = preferenceHelper.get(AUTH_TOKEN)
        return authToken?.let {
            val original: Request = chain.request()
            val builder: Request.Builder = original.newBuilder()
                .header("Authorization", "Bearer $authToken")
            val request: Request = builder.build()
            if (BuildConfig.DEBUG) {
                Timber.d("Current Auth Token #AUTH_TOKEN =====> $authToken")
            }
            return@let chain.proceed(request)
        } ?: chain.proceed(chain.request())
    }

    fun hasToken() = !token.isNullOrEmpty()
}
