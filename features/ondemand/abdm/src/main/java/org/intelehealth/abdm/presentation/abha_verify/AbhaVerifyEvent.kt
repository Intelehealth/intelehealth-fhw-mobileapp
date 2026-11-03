package org.intelehealth.abdm.presentation.abha_verify

import android.os.Parcelable
import androidx.annotation.StringRes
import kotlinx.parcelize.Parcelize
import org.intelehealth.abdm.config.LocalPatientRecord
import org.intelehealth.abdm.result.AbdmResult

/** One-shot events the verify screen reacts to. */
internal sealed interface AbhaVerifyEvent {

    /** Ask the user which OTP channel to use for the ABHA option (Aadhaar-OTP vs Mobile-OTP). */
    data class ShowAuthTypeSelection(val authMethods: List<String>) : AbhaVerifyEvent

    /** Ask the user to pick one ABHA account among several. */
    data class ShowAccountSelection(val accounts: List<AccountChoice>) : AbhaVerifyEvent

    /**
     * Existing local patient → go to the compare screen with both sides already resolved into the
     * same shape (the ABHA side carries the local uuid/openMrsId so the merge saves to the right row).
     */
    data class NavigateToCompare(
        val localRecord: LocalPatientRecord,
        val abhaRecord: LocalPatientRecord,
        val xToken: String,
        val txnId: String,
    ) : AbhaVerifyEvent

    /** New patient (or not on HMIS) → hand the verified profile back to the host. */
    data class CompleteWithResult(val result: AbdmResult) : AbhaVerifyEvent

    /**
     * No ABHA records found for the entered identifier → offer to redirect into the Create flow
     * (legacy parity). [messageRes] is the channel-specific "no records" message.
     */
    data class PromptCreateAbha(@StringRes val messageRes: Int) : AbhaVerifyEvent

    /**
     * One-shot coloured snackbar. Prefers the server-supplied [message]; falls back to [messageRes].
     * [isSuccess] picks the green/red tone.
     */
    data class ShowSnackbar(
        val message: String? = null,
        @StringRes val messageRes: Int? = null,
        val isSuccess: Boolean = false,
    ) : AbhaVerifyEvent
}

/**
 * A selectable ABHA account, shown as a rich row in the picker (legacy parity). [index] is the
 * mobile-search position (used as the index-scope OTP value); [abhaNumber] is used to fetch the
 * profile on the post-verify multi-account path. [isRegisteredLocally] drives the status line.
 */
@Parcelize
internal data class AccountChoice(
    val name: String,
    val abhaNumber: String? = null,
    val gender: String? = null,
    val index: Int? = null,
    val isRegisteredLocally: Boolean = false,
) : Parcelable
