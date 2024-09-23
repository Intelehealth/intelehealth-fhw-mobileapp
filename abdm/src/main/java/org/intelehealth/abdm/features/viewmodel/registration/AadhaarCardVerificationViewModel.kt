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
import org.intelehealth.abdm.common.constant.Constants.BEARER_AUTH
import org.intelehealth.abdm.common.constant.Constants.SCOPE_AADHAAR
import org.intelehealth.abdm.common.di.IoDispatcher
import org.intelehealth.abdm.common.utils.ErrorCode
import org.intelehealth.abdm.common.utils.ErrorCode.CODE_429
import org.intelehealth.abdm.domain.model.AadhaarOtpVerification
import org.intelehealth.abdm.domain.model.request.AadhaarOtpVerificationRequest
import org.intelehealth.abdm.domain.model.request.SendAadhaarOtpApiRequest
import org.intelehealth.abdm.domain.model.request.SendMobileOtpRequest
import org.intelehealth.abdm.domain.model.request.EnrollMobileOtpRequest
import org.intelehealth.abdm.domain.result.ApiResult
import org.intelehealth.abdm.domain.usecase.GetAuthTokenUseCase
import org.intelehealth.abdm.domain.usecase.registration.EnrollMobileOtpUseCase
import org.intelehealth.abdm.domain.usecase.registration.SendAadhaarOtpUseCase
import org.intelehealth.abdm.domain.usecase.registration.SendMobileOtpToEnrollUseCase
import org.intelehealth.abdm.domain.usecase.registration.VerifyAadhaarOtpUseCase
import org.intelehealth.abdm.features.base.BaseViewModel
import org.intelehealth.abdm.features.intent.RegistrationVerificationIntent
import org.intelehealth.abdm.features.viewstate.EnrollMobileOtpViewState
import org.intelehealth.abdm.features.viewstate.SendAadhaarOtpViewState
import org.intelehealth.abdm.features.viewstate.SendMobileOtpToEnrollViewState
import org.intelehealth.abdm.features.viewstate.VerifyAadhaarOtpViewState
import javax.inject.Inject

@HiltViewModel
class AadhaarCardVerificationViewModel @Inject constructor(
    @IoDispatcher
    private val ioDispatcher: CoroutineDispatcher,
    private val getAuthTokenUseCase: GetAuthTokenUseCase,
    private val sendAadhaarOtpUseCase: SendAadhaarOtpUseCase,
    private val verifyAadhaarOtpUseCase: VerifyAadhaarOtpUseCase,
    private val sendMobileOtpToEnrollUseCase: SendMobileOtpToEnrollUseCase,
    private val enrollMobileOtpUseCase: EnrollMobileOtpUseCase,
    @ApplicationContext private val appContext: Context,
) : BaseViewModel() {
    var enteredMobileNumber: String = ""
    private var aadhaarNo: String = ""
    private var accessToken: String? = null
    private var aadhaarOtpVerification: AadhaarOtpVerification? = null
    private var txnId: String = ""

    private val _sendAadhaarOtpState =
        MutableLiveData<SendAadhaarOtpViewState>(SendAadhaarOtpViewState.Idle)
    val sendAadhaarOtpState: LiveData<SendAadhaarOtpViewState>
        get() = _sendAadhaarOtpState

    private val _verifyAadhaarOtpState =
        MutableLiveData<VerifyAadhaarOtpViewState>(VerifyAadhaarOtpViewState.Idle)
    val verifyAadhaarOtpState: LiveData<VerifyAadhaarOtpViewState>
        get() = _verifyAadhaarOtpState

    private val _sendMobileOtpState =
        MutableLiveData<SendMobileOtpToEnrollViewState>(SendMobileOtpToEnrollViewState.Idle)
    val sendMobileOtpState: LiveData<SendMobileOtpToEnrollViewState>
        get() = _sendMobileOtpState

    private val _enrollMobileOtpState =
        MutableLiveData<EnrollMobileOtpViewState>(EnrollMobileOtpViewState.Idle)
    val enrollMobileOtpState: LiveData<EnrollMobileOtpViewState>
        get() = _enrollMobileOtpState


    fun sendIntent(intent: RegistrationVerificationIntent) {
        when (intent) {
            is RegistrationVerificationIntent.OnClickSendOtp -> {
                this.enteredMobileNumber = intent.mobileNo
                this.aadhaarNo = intent.aadhaarNo
                _sendAadhaarOtpState.postValue(SendAadhaarOtpViewState.Loading)
                sendAadhaarOtp(intent.aadhaarNo)
            }

            is RegistrationVerificationIntent.ResendAadhaarOtp -> {
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

            is RegistrationVerificationIntent.OnClickSendMobileOtp -> {
                sendMobileToEnrollOtp(
                    SendMobileOtpRequest(
                        scope = SCOPE_AADHAAR,
                        value = aadhaarNo,
                        txnId = txnId
                    )
                )
            }

            is RegistrationVerificationIntent.ResendMobileOtp -> {

            }

            is RegistrationVerificationIntent.VerifyMobileOtp -> {
                val apiRequest = EnrollMobileOtpRequest(
                    otp = intent.mobileOtp,
                    mobileNo = enteredMobileNumber,
                    txnId = txnId
                )
                verifyAndEnrollMobileNumber(apiRequest)
            }
        }
    }

    private fun getAuthToken() = BEARER_AUTH.plus(accessToken)

    private fun sendAadhaarOtp(aadhaarNo: String) {
        val otpApiRequest =
            SendAadhaarOtpApiRequest(value = aadhaarNo, scope = SCOPE_AADHAAR)
        viewModelScope.launch(ioDispatcher) {
            val authApiResult = getAuthTokenUseCase()

            if (authApiResult is ApiResult.Success) {
                accessToken = authApiResult.data.accessToken
                when (val apiResult = sendAadhaarOtpUseCase(getAuthToken(), otpApiRequest)) {
                    is ApiResult.Success -> {
                        txnId = apiResult.data.txnId ?: ""
                        _sendAadhaarOtpState.postValue(SendAadhaarOtpViewState.Success(apiResult.data))
                    }

                    is ApiResult.Error -> {
                        val error = when (apiResult.code) {
                            CODE_429 -> {
                                appContext.getString(
                                    R.string.you_have_requested_multiple_otps_or_exceeded_maximum_number_of_attempts_for_otp_match_in_this_transaction_please_try_again_in_30_minutes
                                )
                            }

                            else -> {
                                appContext.getString(
                                    R.string.something_went_wrong
                                )
                            }
                        }
                        _sendAadhaarOtpState.postValue(
                            SendAadhaarOtpViewState.Error(
                                error
                            )
                        )
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
                    txnId = apiResult.data.txnId ?: ""
                    _verifyAadhaarOtpState.postValue(
                        VerifyAadhaarOtpViewState.OpenSelectAbhaScreen(
                            apiResult.data
                        )
                    )
                    val otpResponse = apiResult.data
                    val mobile: String = otpResponse.abhaProfile?.mobile ?: ""
                    val isMobileEmpty = TextUtils.isEmpty(mobile)
                    val isNewUser: Boolean = otpResponse.isNew

                    if (isMobileEmpty || !mobile.equals(enteredMobileNumber, ignoreCase = true)) {
                        _verifyAadhaarOtpState.postValue(
                            VerifyAadhaarOtpViewState.OpenMobileVerificationScreen(
                                apiResult.data
                            )
                        )
                    } else {
                        _verifyAadhaarOtpState.postValue(
                            VerifyAadhaarOtpViewState.OpenSelectAbhaScreen(
                                apiResult.data
                            )
                        )
                    }
                }

                is ApiResult.Error -> {
                    val errorMessage = when (apiResult.code) {
                        ErrorCode.CODE_400 -> {
                            appContext.getText(R.string.entered_aadhaar_or_mobile_number_is_incorrect)
                        }

                        ErrorCode.CODE_422 -> {
                            appContext.getText(R.string.please_enter_valid_otp)
                        }

                        else -> {
                            appContext.getText(R.string.something_went_wrong)
                        }
                    }
                    _verifyAadhaarOtpState.postValue(
                        VerifyAadhaarOtpViewState.Error(errorMessage.toString())
                    )
                }
            }
        }
    }


    private fun sendMobileToEnrollOtp(apiRequest: SendMobileOtpRequest) {
        viewModelScope.launch(ioDispatcher) {
            _sendMobileOtpState.postValue(SendMobileOtpToEnrollViewState.Loading)
            when (val apiResult =
                sendMobileOtpToEnrollUseCase(getAuthToken(), apiRequest)) {
                is ApiResult.Success -> {
                    txnId = apiResult.data.txnId ?: ""
                    _sendMobileOtpState.postValue(SendMobileOtpToEnrollViewState.Success(apiResult.data))
                }

                is ApiResult.Error -> {
                    _sendMobileOtpState.postValue(
                        SendMobileOtpToEnrollViewState.Error(
                            appContext.getString(
                                R.string.something_went_wrong
                            )
                        )
                    )
                }
            }
        }
    }

    private fun verifyAndEnrollMobileNumber(apiRequest: EnrollMobileOtpRequest) {
        viewModelScope.launch(ioDispatcher) {
            _enrollMobileOtpState.postValue(EnrollMobileOtpViewState.Loading)
            when (val apiResult =
                enrollMobileOtpUseCase(getAuthToken(), apiRequest)) {
                is ApiResult.Success -> {
                    _enrollMobileOtpState.postValue(
                        EnrollMobileOtpViewState.OpenSelectAbhaScreen(
                            apiResult.data
                        )
                    )
                }

                is ApiResult.Error -> {
                    _enrollMobileOtpState.postValue(
                        EnrollMobileOtpViewState.Error(
                            appContext.getString(
                                R.string.something_went_wrong
                            )
                        )
                    )
                }
            }
        }
    }


}