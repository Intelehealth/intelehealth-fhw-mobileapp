package org.intelehealth.abdm.features.viewmodel.registration

import android.content.Context
import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import org.intelehealth.abdm.R
import org.intelehealth.abdm.common.di.IoDispatcher
import org.intelehealth.abdm.common.utils.ErrorCode.CODE_429
import org.intelehealth.abdm.data.network.ABDMConstant
import org.intelehealth.abdm.data.network.ABDMConstant.BEARER_AUTH
import org.intelehealth.abdm.domain.model.AadhaarOtpVerification
import org.intelehealth.abdm.domain.repository.registration.request.AadhaarOtpVerificationRequest
import org.intelehealth.abdm.domain.repository.registration.request.SendAadhaarOtpApiRequest
import org.intelehealth.abdm.domain.result.ApiResult
import org.intelehealth.abdm.domain.usecase.GetAuthTokenUseCase
import org.intelehealth.abdm.domain.usecase.registration.SendAadhaarOtpUseCase
import org.intelehealth.abdm.domain.usecase.registration.VerifyAadhaarOtpUseCase
import org.intelehealth.abdm.features.base.BaseViewModel
import org.intelehealth.abdm.features.intent.RegistrationVerificationIntent
import org.intelehealth.abdm.features.viewstate.SendAadhaarOtpViewState
import org.intelehealth.abdm.features.viewstate.VerifyAadhaarOtpViewState
import javax.inject.Inject

@HiltViewModel
class AadhaarCardVerificationViewModel @Inject constructor(
    @IoDispatcher
    private val ioDispatcher: CoroutineDispatcher,
    private val getAuthTokenUseCase: GetAuthTokenUseCase,
    private val sendAadhaarOtpUseCase: SendAadhaarOtpUseCase,
    private val verifyAadhaarOtpUseCase: VerifyAadhaarOtpUseCase,
    @ApplicationContext private val appContext: Context
) : BaseViewModel() {
    var enteredMobileNumber: String = ""
    private var accessToken: String? = null
    private var aadhaarOtpVerification: AadhaarOtpVerification? = null

    private val _sendAadhaarOtpState =
        MutableLiveData<SendAadhaarOtpViewState>(SendAadhaarOtpViewState.Idle)
    val sendAadhaarOtpState: LiveData<SendAadhaarOtpViewState>
        get() = _sendAadhaarOtpState

    private val _verifyAadhaarOtpState =
        MutableLiveData<VerifyAadhaarOtpViewState>(VerifyAadhaarOtpViewState.Idle)
    val verifyAadhaarOtpState: LiveData<VerifyAadhaarOtpViewState>
        get() = _verifyAadhaarOtpState

    fun sendIntent(intent: RegistrationVerificationIntent) {
        when (intent) {
            is RegistrationVerificationIntent.OnClickSendOtp -> {
                this.enteredMobileNumber = intent.mobileNo
                _sendAadhaarOtpState.postValue(SendAadhaarOtpViewState.Loading)
                sendAadhaarOtp(intent.aadhaarNo)
            }

            is RegistrationVerificationIntent.OnClickVerifyAadhaarOtp -> {
                val request = AadhaarOtpVerificationRequest().apply {
                    otp = intent.aadhaarOtp
                    txnId = aadhaarOtpVerification?.txnId
                    mobileNo = enteredMobileNumber
                }
                verifyAadhaarOtp(request)
            }

            is RegistrationVerificationIntent.ResendAadhaarOtp -> {}
            is RegistrationVerificationIntent.OnClickMobileVerifyOtp -> {}
            is RegistrationVerificationIntent.ResendMobileVerifyOtp -> {}
        }
    }

    private fun getAuthToken() = BEARER_AUTH.plus(accessToken)

    private fun sendAadhaarOtp(aadhaarNo: String) {
        val otpApiRequest =
            SendAadhaarOtpApiRequest(value = aadhaarNo, scope = ABDMConstant.SCOPE_AADHAAR)
        viewModelScope.launch(ioDispatcher) {
            val authApiResult = getAuthTokenUseCase()

            if (authApiResult is ApiResult.Success) {
                accessToken = authApiResult.data.accessToken
                when (val apiResult = sendAadhaarOtpUseCase(getAuthToken(), otpApiRequest)) {
                    is ApiResult.Success -> {
                        _sendAadhaarOtpState.postValue(SendAadhaarOtpViewState.Success(apiResult.data))
                    }

                    is ApiResult.Error -> {
                        _sendAadhaarOtpState.postValue(SendAadhaarOtpViewState.Error("Auth failed"))
                        when (apiResult.code) {
                            CODE_429 -> {
                                _sendAadhaarOtpState.postValue(
                                    SendAadhaarOtpViewState.Error(
                                        appContext.getString(
                                            R.string.you_have_requested_multiple_otps_or_exceeded_maximum_number_of_attempts_for_otp_match_in_this_transaction_please_try_again_in_30_minutes
                                        )
                                    )
                                )
                            }

                            else -> {
                                _sendAadhaarOtpState.postValue(
                                    SendAadhaarOtpViewState.Error(
                                        appContext.getString(
                                            R.string.something_went_wrong
                                        )
                                    )
                                )

                            }
                        }
                    }
                }

            } else {
                _sendAadhaarOtpState.postValue(
                    SendAadhaarOtpViewState.Error(
                        appContext.getString(
                            R.string.something_went_wrong
                        )
                    )
                )
            }

        }
    }

    private fun verifyAadhaarOtp(apiRequest: AadhaarOtpVerificationRequest) {
        viewModelScope.launch(ioDispatcher) {
            _verifyAadhaarOtpState.postValue(VerifyAadhaarOtpViewState.Loading)
            when (val apiResult =
                verifyAadhaarOtpUseCase(getAuthToken(), apiRequest)) {
                is ApiResult.Success -> {
                    val verificationResponse: AadhaarOtpVerification = apiResult.data
                    val responseMobileNo = verificationResponse.abhaProfile?.mobile
                    val isMobileEmpty = TextUtils.isEmpty(responseMobileNo)
                    val isNewUser: Boolean = verificationResponse.isNew

                    if (isMobileEmpty || !responseMobileNo.equals(enteredMobileNumber, ignoreCase = true)) {
                        _verifyAadhaarOtpState.postValue(VerifyAadhaarOtpViewState.OpenMobileVerificationScreen(apiResult.data))
                    } else {
                        _verifyAadhaarOtpState.postValue(VerifyAadhaarOtpViewState.OpenSelectAbhaScreen(apiResult.data))
                    }
                }

                is ApiResult.Error -> {}
            }
        }
    }
}