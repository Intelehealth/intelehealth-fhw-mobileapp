package org.intelehealth.installer.downloader

import android.content.Context
import android.util.Log
import com.google.android.play.core.splitinstall.SplitInstallException
import com.google.android.play.core.splitinstall.SplitInstallManager
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import com.google.android.play.core.splitinstall.SplitInstallRequest
import com.google.android.play.core.splitinstall.SplitInstallSessionState
import com.google.android.play.core.splitinstall.SplitInstallStateUpdatedListener
import com.google.android.play.core.splitinstall.model.SplitInstallErrorCode
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus

/**
 * Created by Vaghela Mithun R. on 09-10-2024 - 20:21.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/

const val TAG = "dynamic_module_util"

class DynamicModuleDownloadManager(context: Context) {
    private lateinit var splitInstallManager: SplitInstallManager
    private var mySessionId = 0
    private var callback: DynamicDeliveryCallback? = null

    init {
        if (!::splitInstallManager.isInitialized) {
            splitInstallManager = SplitInstallManagerFactory.create(context)
        }
    }

    fun isModuleDownloaded(moduleName: String): Boolean {
        return splitInstallManager.installedModules.contains(moduleName)
    }

    fun downloadDynamicModule(moduleName: String) {
        val request = SplitInstallRequest.newBuilder().addModule(moduleName).build()

        val listener = SplitInstallStateUpdatedListener { state -> handleInstallStates(state) }
        splitInstallManager.registerListener(listener)

        splitInstallManager.startInstall(request).addOnSuccessListener { sessionId ->
            mySessionId = sessionId
        }.addOnFailureListener { e ->
            Log.d(TAG, "Exception: $e")
            handleInstallFailure((e as SplitInstallException).errorCode)
        }

        splitInstallManager.unregisterListener(listener)

    }

    /** Install all features deferred. */
    private fun installAllFeaturesDeferred(modules: List<String>) {
        val listener = SplitInstallStateUpdatedListener { state -> handleInstallStates(state) }
        splitInstallManager.registerListener(listener)
//        val modules = listOf(moduleKotlin, moduleJava, moduleAssets, moduleNative)

        splitInstallManager.deferredInstall(modules).addOnSuccessListener {

        }.addOnFailureListener { e ->
            Log.d(TAG, "Exception: $e")
            handleInstallFailure((e as SplitInstallException).errorCode)
        }

        splitInstallManager.unregisterListener(listener)
    }

    /** Request uninstall of all features. */
    private fun requestUninstall() {

        Log.d(
            TAG, "Requesting uninstall of all modules. This will happen at some point in the future."
        )

        val installedModules = splitInstallManager.installedModules.toList()
        splitInstallManager.deferredUninstall(installedModules).addOnSuccessListener {
            Log.d(TAG, "Uninstalling $installedModules")
        }
    }

    /** Request uninstall of all features. */
    private fun requestUninstall(modules: List<String>) {
        splitInstallManager.deferredUninstall(modules).addOnSuccessListener {
            Log.d(TAG, "Uninstalling $modules")
        }
    }

    private fun handleInstallFailure(errorCode: Int) {
        when (errorCode) {
            SplitInstallErrorCode.NETWORK_ERROR -> {
                callback?.onFailed("No internet found")
            }

            SplitInstallErrorCode.MODULE_UNAVAILABLE -> {
                callback?.onFailed("Module unavailable")
            }

            SplitInstallErrorCode.ACTIVE_SESSIONS_LIMIT_EXCEEDED -> {
                callback?.onFailed("Active session limit exceeded")
            }

            SplitInstallErrorCode.INSUFFICIENT_STORAGE -> {
                callback?.onFailed("Insufficient storage")
            }

            SplitInstallErrorCode.PLAY_STORE_NOT_FOUND -> {
                callback?.onFailed("Google Play Store Not Found!")
            }

            else -> {
                callback?.onFailed("Something went wrong! Try again later")
            }
        }
    }

    private fun handleInstallStates(state: SplitInstallSessionState) {
        if (state.sessionId() == mySessionId) {
            when (state.status()) {
                SplitInstallSessionStatus.DOWNLOADING -> {
                    callback?.onDownloading()
                }

                SplitInstallSessionStatus.DOWNLOADED -> {
                    callback?.onDownloadCompleted()
                }

                SplitInstallSessionStatus.INSTALLED -> {
                    Log.d(TAG, "Dynamic Module downloaded")
                    callback?.onInstallSuccess()
                }

                SplitInstallSessionStatus.FAILED -> {
                    callback?.onFailed("Installation failed")
                }

                SplitInstallSessionStatus.CANCELED -> {
                    callback?.onFailed("Installation Cancelled")
                }
            }
        }
    }
}