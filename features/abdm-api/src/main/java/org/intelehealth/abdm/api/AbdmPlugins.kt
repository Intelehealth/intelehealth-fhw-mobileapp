package org.intelehealth.abdm.api

import android.util.Log

/**
 * Locates the ABDM plugin at runtime. The implementation module is optional:
 * when it is not bundled in the current flavor, [plugin] is null and every
 * ABHA entry point should stay hidden.
 */
object AbdmPlugins {
    private const val TAG = "AbdmPlugins"
    private const val IMPLEMENTATION = "org.intelehealth.abdm.AbdmPluginImpl"

    @JvmStatic
    val plugin: AbdmPlugin? by lazy {
        try {
            Class.forName(IMPLEMENTATION).getDeclaredConstructor().newInstance() as AbdmPlugin
        } catch (e: ClassNotFoundException) {
            Log.i(TAG, "ABDM module not bundled in this build")
            null
        } catch (e: Exception) {
            Log.e(TAG, "Unable to load ABDM plugin", e)
            null
        }
    }

    @JvmStatic
    val isBundled: Boolean get() = plugin != null

    /** ABHA is usable only when the module is bundled AND the server config enables it. */
    @JvmStatic
    fun isEnabled(serverFlag: Boolean): Boolean = serverFlag && isBundled
}
