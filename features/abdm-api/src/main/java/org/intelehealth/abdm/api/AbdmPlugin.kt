package org.intelehealth.abdm.api

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity

/**
 * Everything the app needs from the ABDM (ABHA) feature.
 *
 * The app never references the implementation module directly; it obtains an
 * instance through [AbdmPlugins.plugin], which is null when the module is not
 * bundled in the current flavor.
 */
interface AbdmPlugin {

    /** Passes session/server details to the plugin. Call after login, before any flow. */
    fun initialize(config: AbdmConfig)

    /** Shows the "Has ABHA / Create ABHA / Continue without ABHA" choice. */
    fun showAbhaChoice(activity: AppCompatActivity, callbacks: AbhaChoiceCallbacks)

    /**
     * Asks for the patient's name, then starts [flow]. The outcome is delivered to
     * [launcher]; pass its result Intent to [parseResult].
     */
    fun startFlow(
        activity: AppCompatActivity,
        flow: AbdmFlow,
        launcher: ActivityResultLauncher<Intent>
    )

    /** Converts a flow's result Intent into app-level data, or null if there is none. */
    fun parseResult(data: Intent?): AbdmFlowResult?

    /**
     * Downloads the ABHA card for [abhaNumber] into [AbhaCardStore] when it is
     * missing or out of date. Runs asynchronously; failures are logged.
     */
    fun downloadAbhaCardIfNeeded(context: Context, abhaNumber: String?, session: AbdmSession)
}

interface AbhaChoiceCallbacks {
    fun onVerifyAbha()
    fun onCreateAbha()
    fun onContinueWithoutAbha()
}
