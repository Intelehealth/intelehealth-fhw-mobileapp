package org.intelehealth.abdm.api

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner

/**
 * Everything the host app may ask of the ABHA/ABDM feature.
 *
 * The host never references the implementation module directly; it asks Hilt for an optional
 * [AbhaFeature]. A client build that does not ship :features:ondemand:abdm simply has no binding,
 * and every ABHA entry point in the host stays hidden. Whether the feature is also switched on at
 * runtime (the `abha_section` server flag) is the host's concern, not this interface's.
 */
interface AbhaFeature {

    /**
     * Listens for the Verify / Create / Continue-without choice. Register in onCreate so the
     * answer survives a configuration change while the dialog is open.
     */
    fun registerChoiceListener(
        activity: FragmentActivity,
        owner: LifecycleOwner,
        onChoice: (AbhaChoice) -> Unit,
    )

    fun showChoice(activity: FragmentActivity)

    /** Asks for the patient name, then runs the verify flow; the result is an [org.intelehealth.abdm.result.AbdmResult]. */
    fun startVerifyAbha(activity: FragmentActivity, resultLauncher: ActivityResultLauncher<Intent>)

    /** Asks for the patient name, then runs the create flow; the result is an [org.intelehealth.abdm.result.AbdmResult]. */
    fun startCreateAbha(activity: FragmentActivity, resultLauncher: ActivityResultLauncher<Intent>)

    /** Adds a further ABHA address to an already-resolved ABHA; read the answer with [chosenAbhaAddress]. */
    fun startAbhaSuggestions(
        activity: FragmentActivity,
        resultLauncher: ActivityResultLauncher<Intent>,
        txnId: String,
        currentAddress: String,
        patientUuid: String,
    )

    /** The address picked on the suggestions screen, or null when nothing was chosen. */
    fun chosenAbhaAddress(data: Intent?): String?

    fun downloadCardInBackground(context: Context, xToken: String?, cardScope: String?, abhaNumber: String?)

    fun invalidateCard(context: Context, abhaNumber: String?)

    fun viewCard(context: Context, abhaNumber: String?)
}

enum class AbhaChoice { VERIFY_ABHA, CREATE_ABHA, CONTINUE_WITHOUT_ABHA }
