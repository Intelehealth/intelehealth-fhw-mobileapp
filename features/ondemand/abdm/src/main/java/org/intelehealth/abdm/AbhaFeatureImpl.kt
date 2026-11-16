package org.intelehealth.abdm

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import org.intelehealth.abdm.api.AbhaChoice
import org.intelehealth.abdm.api.AbhaFeature
import org.intelehealth.abdm.presentation.AbdmCardDownloader
import org.intelehealth.abdm.presentation.AbdmLauncher
import org.intelehealth.abdm.presentation.abha_choice.AbhaChoiceDialogFragment
import org.intelehealth.abdm.presentation.abha_suggestions.AbhaSuggestionsActivity
import javax.inject.Inject
import javax.inject.Singleton

/** The real [AbhaFeature], bound into the host's Hilt graph by [AbhaFeatureModule]. */
@Singleton
internal class AbhaFeatureImpl @Inject constructor() : AbhaFeature {

    override fun registerChoiceListener(
        activity: FragmentActivity,
        owner: LifecycleOwner,
        onChoice: (AbhaChoice) -> Unit,
    ) {
        AbhaChoiceDialogFragment.setResultListener(activity.supportFragmentManager, owner) { choice ->
            onChoice(
                when (choice) {
                    AbhaChoiceDialogFragment.Choice.VERIFY_ABHA -> AbhaChoice.VERIFY_ABHA
                    AbhaChoiceDialogFragment.Choice.CREATE_ABHA -> AbhaChoice.CREATE_ABHA
                    AbhaChoiceDialogFragment.Choice.CONTINUE_WITHOUT_ABHA -> AbhaChoice.CONTINUE_WITHOUT_ABHA
                }
            )
        }
    }

    override fun showChoice(activity: FragmentActivity) =
        AbhaChoiceDialogFragment.show(activity.supportFragmentManager)

    override fun startVerifyAbha(activity: FragmentActivity, resultLauncher: ActivityResultLauncher<Intent>) =
        AbdmLauncher.startVerifyAbha(activity, resultLauncher)

    override fun startCreateAbha(activity: FragmentActivity, resultLauncher: ActivityResultLauncher<Intent>) =
        AbdmLauncher.startCreateAbha(activity, resultLauncher)

    override fun startAbhaSuggestions(
        activity: FragmentActivity,
        resultLauncher: ActivityResultLauncher<Intent>,
        txnId: String,
        currentAddress: String,
        patientUuid: String,
    ) = AbdmLauncher.startAbhaSuggestions(activity, resultLauncher, txnId, currentAddress, patientUuid)

    override fun chosenAbhaAddress(data: Intent?): String? =
        data?.getStringExtra(AbhaSuggestionsActivity.EXTRA_CHOSEN_ADDRESS)?.takeIf { it.isNotBlank() }

    override fun downloadCardInBackground(
        context: Context,
        xToken: String?,
        cardScope: String?,
        abhaNumber: String?,
    ) = AbdmCardDownloader.downloadInBackground(context, xToken, cardScope, abhaNumber)

    override fun invalidateCard(context: Context, abhaNumber: String?) =
        AbdmCardDownloader.invalidate(context, abhaNumber)

    override fun viewCard(context: Context, abhaNumber: String?) =
        AbdmCardDownloader.viewCard(context, abhaNumber)
}
