package org.intelehealth.videolibrary.utils

/**
 * Created by Arpan Sircar. on 08-02-2024.
 * Email : arpan@intelehealth.org
 * Mob   : +919123116015
 **/

object VideoLibraryManager {

    // variable of type String
    private var baseUrl = ""

    fun getBaseUrlWithPort(): String {
        return "https://${getBaseUrl()}:3004"
    }

    @JvmStatic
    fun getBaseUrl(): String {
        return baseUrl
    }

    @JvmStatic
    fun setBaseUrl(baseUrl: String) {
        this.baseUrl = baseUrl
    }
}