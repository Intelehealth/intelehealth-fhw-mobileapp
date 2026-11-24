package org.intelehealth.abdm.result

import android.content.Intent
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * The result the ABDM flow returns to the host via the Activity result intent.
 * The host reads [EXTRA_ABDM_RESULT], switches on [outcome], and uses [profile] to continue
 * patient registration.
 */
@Parcelize
data class AbdmResult(
    val outcome: AbdmOutcomes,
    val accessToken: String?,
    val xToken: String?,
    val txnId: String?,
    val isNew: Boolean,
    val profile: AbdmAbhaProfile?,
    val uuid: String? = null,
    val openMrsId: String? = null,
    val cardScope: String? = null,
) : Parcelable {
    companion object {
        /** Intent-extra key for the parcelled [AbdmResult]. Value kept verbatim from legacy. */
        const val EXTRA_ABDM_RESULT = "abdm_result"

        /** getCard scope by originating flow (legacy: aadhaar for create, mobile for verify). */
        const val CARD_SCOPE_CREATE = "aadhar"
        const val CARD_SCOPE_VERIFY = "mobile"

        /**
         * Sent with a cancelled result: true if the consent dialog's last answer was Decline,
         * false if it was Accept. Absent when no answer was given.
         */
        const val EXTRA_CONSENT_DECLINED = "abdm_consent_declined"

        @JvmStatic
        fun consentAnswerIntent(declined: Boolean): Intent =
            Intent().putExtra(EXTRA_CONSENT_DECLINED, declined)

        /** The consent answer a cancelled ABDM screen sent back, or null if it gave none. */
        @JvmStatic
        fun consentDeclined(data: Intent?): Boolean? =
            data?.takeIf { it.hasExtra(EXTRA_CONSENT_DECLINED) }
                ?.getBooleanExtra(EXTRA_CONSENT_DECLINED, false)
    }
}
