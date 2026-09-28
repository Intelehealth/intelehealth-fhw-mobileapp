package org.intelehealth.abdm.result

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
    }
}
