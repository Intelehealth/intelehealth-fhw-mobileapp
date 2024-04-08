package org.intelehealth.app.utilities

/**
 * Created by Tanvir Hasan on 28-03-2024 : 12-16.
 * Email: mhasan@intelehealth.org
 */
interface WebViewStatus {
    fun onPageStarted()
    fun onPageFinish()

    fun onPageError(error:String)
}