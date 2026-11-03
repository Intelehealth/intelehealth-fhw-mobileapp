package org.intelehealth.abdm.presentation.abha_create

import androidx.annotation.StringRes
import org.intelehealth.abdm.domain.model.AbhaCreateSession
import org.intelehealth.abdm.presentation.common.UiState

internal data class AbhaCreateUiState(
    val step: AbhaCreateStep = AbhaCreateStep.EnterAadhaar,
    val patientName: String = "",
    val isConsentChecked: Boolean = false,
    val aadhaarLocked: Boolean = false,
    val aadhaarNumber: String = "",
    val aadhaarOtp: String = "",
    val mobileNumber: String = "",
    val mobileOtp: String = "",
    val txnId: String? = null,
    val operation: UiState<Unit> = UiState.Idle,

    @StringRes val loadingMessageRes: Int? = null,

    val verifiedSession: AbhaCreateSession? = null,

    @StringRes val aadhaarError: Int? = null,
    @StringRes val mobileError: Int? = null,
    @StringRes val otpError: Int? = null,

    val resendSecondsRemaining: Int = 0,
    val resendAttemptsRemaining: Int = MAX_RESEND_ATTEMPTS,
) {
    companion object {
        const val MAX_RESEND_ATTEMPTS = 2
    }
}