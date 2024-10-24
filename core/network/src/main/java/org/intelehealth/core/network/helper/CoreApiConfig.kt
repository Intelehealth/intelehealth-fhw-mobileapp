package org.intelehealth.core.network.helper

import android.content.Context
import androidx.preference.PreferenceManager
import org.intelehealth.app.BuildConfig



/**
 * Created by - Prajwal W. on 10/10/24.
 * Email: prajwalwaingankar@gmail.com
 * Mobile: +917304154312
 **/

object CoreApiConfig {
    private const val BASE_URL_KEY = "base_url"
    private const val PORT_KEY = "port_number"
    lateinit var baseUrl: String

    class Builder(baseUrl: String) {
        init {
            CoreApiConfig.baseUrl = baseUrl
        }
    }

    fun setBaseUrlAndPort(context: Context, baseUrl: String, port: String) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit()
            .putString(BASE_URL_KEY, baseUrl)
            .putString(PORT_KEY, port)
            .apply()
    }

    fun getBaseUrl(context: Context): String {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val baseUrl = prefs.getString(BASE_URL_KEY, BuildConfig.SERVER_URL) ?: BuildConfig.SERVER_URL
        val port = prefs.getString(PORT_KEY, "443") ?: "443"
        return "$baseUrl:$port/"
    }
}