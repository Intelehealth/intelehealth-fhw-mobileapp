package org.intelehealth.abdm

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.IntentCompat
import org.intelehealth.abdm.abha_create.CreateAbhaAccountActivity
import org.intelehealth.abdm.abha_verify.AbhaCardVerificationActivity
import org.intelehealth.abdm.api.AbdmConfig
import org.intelehealth.abdm.api.AbdmFlow
import org.intelehealth.abdm.api.AbdmFlowResult
import org.intelehealth.abdm.api.AbdmNavigation
import org.intelehealth.abdm.api.AbdmPlugin
import org.intelehealth.abdm.api.AbdmSession
import org.intelehealth.abdm.api.AbhaChoiceCallbacks
import org.intelehealth.abdm.constants.AbdmConstant
import org.intelehealth.abdm.dialog.AbhaChoiceDialogFragment
import org.intelehealth.abdm.enums.AbdmOutcomes
import org.intelehealth.abdm.listener.AbhaChoiceListener
import org.intelehealth.abdm.model.AbdmResult
import org.intelehealth.abdm.utils.AbdmManager
import org.intelehealth.abdm.utils.AbhaCardDownloader
import org.intelehealth.abdm.utils.AbhaPatientMapper
import org.intelehealth.abdm.utils.DialogUtils

/**
 * Entry point of the ABDM module. Loaded by class name from
 * [org.intelehealth.abdm.api.AbdmPlugins]; keep the name and no-arg constructor.
 */
class AbdmPluginImpl : AbdmPlugin {

    override fun initialize(config: AbdmConfig) {
        AbdmManager.baseUrl = config.baseUrl
        AbdmManager.encoded = config.encodedAuth
        AbdmManager.locationUuid = config.locationUuid
        AbdmManager.hwFullName = config.hwFullName
        AbdmManager.dbClient = config.database
        AbdmManager.abhaAddressSuffix = config.abhaAddressSuffix
    }

    override fun showAbhaChoice(activity: AppCompatActivity, callbacks: AbhaChoiceCallbacks) {
        AbhaChoiceDialogFragment().apply {
            listener = object : AbhaChoiceListener {
                override fun onHasAbha() = callbacks.onVerifyAbha()
                override fun onCreateAbha() = callbacks.onCreateAbha()
                override fun onContinueWithoutAbha() = callbacks.onContinueWithoutAbha()
            }
        }.show(activity.supportFragmentManager, AbhaChoiceDialogFragment.TAG)
    }

    override fun startFlow(
        activity: AppCompatActivity,
        flow: AbdmFlow,
        launcher: ActivityResultLauncher<Intent>
    ) {
        val target = when (flow) {
            AbdmFlow.VERIFY_ABHA -> AbhaCardVerificationActivity::class.java
            AbdmFlow.CREATE_ABHA -> CreateAbhaAccountActivity::class.java
        }
        DialogUtils.triggerTextViewDialogFragment(
            activity,
            target,
            AbhaCardVerificationActivity.intentPatientNameTag,
            launcher
        )
    }

    override fun parseResult(data: Intent?): AbdmFlowResult? {
        data ?: return null
        val result = IntentCompat.getParcelableExtra(
            data, AbdmConstant.INTENT_ABDM_RESULT, AbdmResult::class.java
        ) ?: return null

        val otpResponse = result.otpResponse
        val abhaResponse = result.abhaResponse
        val session = AbdmSession(
            accessToken = result.accessToken,
            cardToken = AbhaCardDownloader.cardToken(otpResponse, abhaResponse, result.xToken),
            cardScope = AbhaCardDownloader.cardScope(otpResponse, abhaResponse)
        )
        val details = when {
            otpResponse != null -> AbhaPatientMapper.fromOtpResponse(otpResponse)
            abhaResponse != null -> AbhaPatientMapper.fromAbhaProfile(abhaResponse)
            else -> null
        }

        return when (result.outcome) {
            AbdmOutcomes.NAVIGATE_TO_IDENTIFICATION_SCREEN_WITH_EXISTING_DETAILS_FOR_CREATION ->
                AbdmFlowResult(AbdmNavigation.PATIENT_REGISTRATION, otpResponse?.uuID, false, details, session)

            AbdmOutcomes.NAVIGATE_TO_IDENTIFICATION_SCREEN_FOR_NEW_PATIENT_FOR_CREATION ->
                AbdmFlowResult(AbdmNavigation.PATIENT_REGISTRATION, null, false, details, session)

            AbdmOutcomes.NAVIGATE_TO_IDENTIFICATION_SCREEN_AFTER_ABHA_SUGGESTIONS_FOR_CREATION ->
                AbdmFlowResult(AbdmNavigation.PATIENT_REGISTRATION, otpResponse?.uuID, true, details, session)

            AbdmOutcomes.NAVIGATE_TO_IDENTIFICATION_SCREEN_WITH_NEW_PATIENT_FOR_VERIFICATION ->
                AbdmFlowResult(AbdmNavigation.PATIENT_REGISTRATION, null, false, details, session)

            AbdmOutcomes.NAVIGATE_TO_PATIENT_DETAILS_SCREEN_WITH_EXISTING_PATIENT_AFTER_COMPARISON ->
                AbdmFlowResult(AbdmNavigation.PATIENT_DETAILS, abhaResponse?.uuiD, true, details, session)
        }
    }

    override fun downloadAbhaCardIfNeeded(
        context: Context,
        abhaNumber: String?,
        session: AbdmSession
    ) {
        AbhaCardDownloader.downloadIfNeeded(context, abhaNumber, session)
    }
}
