package org.intelehealth.installer.downloader

import android.content.Context
import android.os.CountDownTimer
import android.util.Log
import com.google.android.play.core.splitinstall.SplitInstallException
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import com.google.android.play.core.splitinstall.SplitInstallRequest
import com.google.android.play.core.splitinstall.SplitInstallSessionState
import com.google.android.play.core.splitinstall.SplitInstallStateUpdatedListener
import com.google.android.play.core.splitinstall.model.SplitInstallErrorCode
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus
import org.intelehealth.installer.helper.DownloadProgressNotificationHelper

/**
 * Created by Vaghela Mithun R. on 09-10-2024 - 20:21.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/

const val TAG = "dynamic_module_manager"

class DynamicModuleDownloadManager private constructor(context: Context) {

    companion object {
        @Volatile
        private var instance: DynamicModuleDownloadManager? = null

        @JvmStatic
        fun getInstance(context: Context): DynamicModuleDownloadManager = instance ?: synchronized(this) {
            instance ?: DynamicModuleDownloadManager(context).also { instance = it }
        }
    }

    private var mySessionId = 0

    private val splitInstallManager by lazy {
        SplitInstallManagerFactory.create(context)
    }

    private val downloadProgressHelper by lazy {
        DownloadProgressNotificationHelper.getInstance(context)
    }

    fun isModuleDownloaded(moduleName: String): Boolean {
        println("${TAG}=>isModuleDownloaded=>$moduleName")
        return splitInstallManager.installedModules.contains(moduleName)
    }

    fun showDownloadingNotification() {
        downloadProgressHelper.setTitle("Intelehealth")
        downloadProgressHelper.setContent("Downloading...")
        downloadProgressHelper.startNotifying()

        object : CountDownTimer(10000, 1000) {
            override fun onTick(p0: Long) {
                val progress = 100 - ((p0 * 100) / 10000).toInt()
                Log.e("FeatureDownloadService ", "Interval $progress ==> $p0")
                downloadProgressHelper.updateProgress(progress)
            }

            override fun onFinish() {
                downloadProgressHelper.setContent("Download complete")
                downloadProgressHelper.completeProgress()
            }

        }.start()
    }

    private fun initNotification() {
        println("${TAG}=>initNotification")
        downloadProgressHelper.setTitle("Intelehealth")
        downloadProgressHelper.setContent("Downloading...")
        downloadProgressHelper.startNotifying()
    }

    fun downloadDynamicModule(moduleName: String, callback: DynamicDeliveryCallback?) {
        val request = SplitInstallRequest.newBuilder().addModule(moduleName).build()
        println("${TAG}=>downloadDynamicModule=>$moduleName")
        initNotification()
        val listener = SplitInstallStateUpdatedListener { state -> handleInstallStates(state, callback) }
        splitInstallManager.registerListener(listener)

        splitInstallManager.startInstall(request).addOnSuccessListener { sessionId ->
            mySessionId = sessionId
        }.addOnFailureListener { e ->
            Log.d(TAG, "Exception: $e")
            handleInstallFailure((e as SplitInstallException).errorCode, callback)
        }

        splitInstallManager.unregisterListener(listener)

    }

    /** Install all features deferred. */
    fun installAllFeaturesDeferred(modules: List<String>, callback: DynamicDeliveryCallback?) {
        val listener = SplitInstallStateUpdatedListener { state -> handleInstallStates(state, callback) }
        splitInstallManager.registerListener(listener)
//        val modules = listOf(moduleKotlin, moduleJava, moduleAssets, moduleNative)
        initNotification()
        splitInstallManager.deferredInstall(modules).addOnSuccessListener {

        }.addOnFailureListener { e ->
            Log.d(TAG, "Exception: $e")
            handleInstallFailure((e as SplitInstallException).errorCode, callback)
        }

        splitInstallManager.unregisterListener(listener)
    }

    /** Request uninstall of all features. */
    fun requestUninstall() {

        Log.d(
            TAG, "Requesting uninstall of all modules. This will happen at some point in the future."
        )

        val installedModules = splitInstallManager.installedModules.toList()
        splitInstallManager.deferredUninstall(installedModules).addOnSuccessListener {
            Log.d(TAG, "Uninstalling $installedModules")
        }
    }

    /** Request uninstall of all features. */
    fun requestUninstall(modules: List<String>) {
        splitInstallManager.deferredUninstall(modules).addOnSuccessListener {
            Log.d(TAG, "Uninstalling $modules")
            println("${TAG}=>Uninstalling $modules")
        }
    }


    private fun handleInstallFailure(errorCode: Int, callback: DynamicDeliveryCallback?) {
        when (errorCode) {
            SplitInstallErrorCode.NETWORK_ERROR -> {
                println("${TAG}=>NETWORK_ERROR")
                callback?.onFailed("No internet found")
                cancelNotificationWithMessage("No internet found")
            }

            SplitInstallErrorCode.MODULE_UNAVAILABLE -> {
                println("${TAG}=>MODULE_UNAVAILABLE")
                callback?.onFailed("Module unavailable")
                cancelNotificationWithMessage("Module unavailable")
            }

            SplitInstallErrorCode.ACTIVE_SESSIONS_LIMIT_EXCEEDED -> {
                println("${TAG}=>ACTIVE_SESSIONS_LIMIT_EXCEEDED")
                callback?.onFailed("Active session limit exceeded")
                cancelNotificationWithMessage("Active session limit exceeded")
            }

            SplitInstallErrorCode.INSUFFICIENT_STORAGE -> {
                println("${TAG}=>INSUFFICIENT_STORAGE")
                callback?.onFailed("Insufficient storage")
                cancelNotificationWithMessage("Insufficient storage")
            }

            SplitInstallErrorCode.PLAY_STORE_NOT_FOUND -> {
                println("${TAG}=>PLAY_STORE_NOT_FOUND")
                callback?.onFailed("Google Play Store Not Found!")
                cancelNotificationWithMessage("Google Play Store Not Found!")
            }

            else -> {
                println("${TAG}=>Something went wrong! Try again later")
                callback?.onFailed("Something went wrong! Try again later")
                cancelNotificationWithMessage("Something went wrong! Try again later")
            }
        }
    }

    private fun handleInstallStates(state: SplitInstallSessionState, callback: DynamicDeliveryCallback?) {
        if (state.sessionId() == mySessionId) {
            when (state.status()) {
                SplitInstallSessionStatus.DOWNLOADING -> {
                    val percentage = (state.bytesDownloaded() * 100) / state.totalBytesToDownload()
                    println("${TAG}=>DOWNLOADING=>$percentage")
                    callback?.onDownloading(percentage.toInt())
                    downloadProgressHelper.updateProgress(percentage.toInt())
                }

                SplitInstallSessionStatus.DOWNLOADED -> {
                    println("${TAG}=>DOWNLOADED")
                    callback?.onDownloadCompleted()
                    downloadProgressHelper.setContent("Installing...")
                    downloadProgressHelper.completeProgress()
                }

                SplitInstallSessionStatus.INSTALLED -> {
                    println("${TAG}=>INSTALLED")
                    Log.d(TAG, "Dynamic Module downloaded")
                    callback?.onInstallSuccess()
                    cancelNotificationWithMessage("Installation complete")
                }

                SplitInstallSessionStatus.FAILED -> {
                    println("${TAG}=>FAILED")
                    callback?.onFailed("Installation failed")
                    cancelNotificationWithMessage("Installation failed")
                }

                SplitInstallSessionStatus.CANCELED -> {
                    println("${TAG}=>CANCELED")
                    callback?.onFailed("Installation Cancelled")
                    cancelNotificationWithMessage("Installation Cancelled")
                }
            }
        }
    }

    private fun cancelNotificationWithMessage(message: String) {
        downloadProgressHelper.setContent(message)
        downloadProgressHelper.startNotifying()
        downloadProgressHelper.cancelWithDelay(1000)
    }
}