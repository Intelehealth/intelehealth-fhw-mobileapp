package org.intelehealth.installer.downloader

/**
 * Created by Vaghela Mithun R. on 09-10-2024 - 20:19.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
interface DynamicDeliveryCallback {
    fun onDownloading(percentage: Int)
    fun onDownloadCompleted()
    fun onInstallSuccess()
    fun onFailed(errorMessage: String)
}