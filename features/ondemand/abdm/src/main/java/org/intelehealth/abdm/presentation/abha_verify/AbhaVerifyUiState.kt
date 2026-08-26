package org.intelehealth.abdm.presentation.abha_verify

import androidx.annotation.StringRes
import org.intelehealth.abdm.presentation.common.UiState

internal data class AbhaVerifyUiState(
    val method: AbhaVerifyMethod = AbhaVerifyMethod.Aadhaar,
    val step: AbhaVerifyStep = AbhaVerifyStep.EnterDetails,
    val patientName: String = "",

    val isConsentChecked: Boolean = false,

    val aadhaarNumber: String = "",
    val mobileNumber: String = "",
    val abhaNumber: String = "",
    val abhaAddress: String = "",
    val otp: String = "",

    val txnId: String? = null,

    val operation: UiState<Unit> = UiState.Idle,
    @StringRes val loadingMessageRes: Int? = null,

    @StringRes val aadhaarError: Int? = null,
    @StringRes val mobileError: Int? = null,
    @StringRes val abhaError: Int? = null,
    @StringRes val otpError: Int? = null,

    val resendSecondsRemaining: Int = 0,
    val resendAttemptsRemaining: Int = MAX_RESEND_ATTEMPTS,
) {
    companion object {
        const val MAX_RESEND_ATTEMPTS = 2
    }
}
