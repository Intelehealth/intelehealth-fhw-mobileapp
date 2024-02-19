package org.intelehealth.app.networkApiCalls.interceptors

import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.internal.closeQuietly
import org.intelehealth.app.R
import org.intelehealth.app.app.IntelehealthApplication
import org.intelehealth.app.utilities.NavigationUtils
import java.io.IOException

/**
 * Created by Tanvir Hasan on 18-02-2024 : 00-35.
 * Email: mhasan@intelehealth.org
 *
 * Created this interceptor to logout while any 401 status code thrown
 */
class LogoutInterceptor :Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        //Not going to check 401 on some api which port is not 3004
        //that's why added the logic
        if (chain.request().url.port == 3004) {
            val response = chain.proceed(chain.request())
            response.closeQuietly()
            if(response.code == 401){
                //do logout
                val navigationUtils = NavigationUtils()
                navigationUtils.triggerSignOutOn401Response(IntelehealthApplication.getInstance())
                throw LogoutException()
            }
        }
        return chain.proceed(chain.request())
    }
}

/**
 * New exception to detect logout
 */
class LogoutException : IOException() {
    override val message: String
        get() = IntelehealthApplication.getInstance().getString(R.string.token_expired)
}