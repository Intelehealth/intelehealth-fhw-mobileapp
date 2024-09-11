package org.intelehealth.abdm.features.viewmodel.registration

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.intelehealth.abdm.features.base.BaseViewModel
import org.intelehealth.abdm.features.intent.RegistrationVerificationIntent
import org.intelehealth.abdm.features.viewstate.SendAadhaarOtpViewState

class AadhaarCardVerificationViewModel : BaseViewModel() {

    private val _sendAadhaarOtpState = MutableStateFlow<SendAadhaarOtpViewState>(SendAadhaarOtpViewState.Idle)
    val sendAadhaarOtpState: StateFlow<SendAadhaarOtpViewState>
        get() = _sendAadhaarOtpState.asStateFlow()
    fun sendIntent(intent: RegistrationVerificationIntent) {
        when (intent) {
            is RegistrationVerificationIntent.OnClickSendOtp -> {}
            is RegistrationVerificationIntent.OnClickVerifyAadhaarOtp -> {}
            is RegistrationVerificationIntent.ResendAadhaarOtp -> {}
            is RegistrationVerificationIntent.OnClickMobileVerifyOtp -> {}
            is RegistrationVerificationIntent.ResendMobileVerifyOtp -> {}
        }
    }

}