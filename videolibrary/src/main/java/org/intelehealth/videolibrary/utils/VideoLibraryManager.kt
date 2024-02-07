package org.intelehealth.videolibrary.utils

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