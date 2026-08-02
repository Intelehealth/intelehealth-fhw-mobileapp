package org.intelehealth.abdm.presentation

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.FragmentActivity
import org.intelehealth.abdm.presentation.abha_create.AbhaCreateActivity
import org.intelehealth.abdm.presentation.abha_create.PatientNameDialogFragment
import org.intelehealth.abdm.presentation.abha_verify.AbhaVerifyActivity

/**
 * Host entry points for the ABDM flows. The patient name is collected once, up front (mirroring
 * legacy DialogUtils.triggerTextViewDialogFragment), then handed to whichever flow the host chose,
 * so Verify, Create, and a Verify to Create redirect all carry the same name without re-prompting.
 */
object AbdmLauncher {

    @JvmStatic
    fun startVerifyAbha(activity: FragmentActivity, resultLauncher: ActivityResultLauncher<Intent>) {
        promptNameThenLaunch(
            activity,
            AbhaVerifyActivity::class.java,
            AbhaVerifyActivity.EXTRA_PATIENT_NAME,
            resultLauncher,
        )
    }

    @JvmStatic
    fun startCreateAbha(activity: FragmentActivity, resultLauncher: ActivityResultLauncher<Intent>) {
        promptNameThenLaunch(
            activity,
            AbhaCreateActivity::class.java,
            AbhaCreateActivity.EXTRA_PATIENT_NAME,
            resultLauncher,
        )
    }

    private fun promptNameThenLaunch(
        activity: FragmentActivity,
        target: Class<*>,
        extraKey: String,
        resultLauncher: ActivityResultLauncher<Intent>,
    ) {
        PatientNameDialogFragment.setResultListener(
            activity.supportFragmentManager,
            activity,
            onName = { name ->
                resultLauncher.launch(Intent(activity, target).putExtra(extraKey, name))
            },
            onCancelled = {},
        )
        PatientNameDialogFragment.show(activity.supportFragmentManager)
    }
}
