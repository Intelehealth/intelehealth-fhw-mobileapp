package org.intelehealth.abdm.presentation.abha_create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.intelehealth.abdm.R
import org.intelehealth.abdm.config.AbdmConfig
import org.intelehealth.abdm.config.AbdmPatientLocalStore
import org.intelehealth.abdm.config.AbdmSessionProvider
import org.intelehealth.abdm.data.remote.extensions.HttpException
import org.intelehealth.abdm.data.remote.extensions.OtpVerificationFailedException
import org.intelehealth.abdm.domain.model.AbhaCreateSession
import org.intelehealth.abdm.domain.repository.AbhaCreateRepository
import org.intelehealth.abdm.domain.repository.PatientRepository
import org.intelehealth.abdm.presentation.common.UiState
import org.intelehealth.abdm.result.AbdmOutcomes
import org.intelehealth.abdm.util.VerhoeffAlgorithm
import javax.inject.Inject

@HiltViewModel
internal class AbhaCreateViewModel @Inject constructor(
    private val createRepository: AbhaCreateRepository,
    private val patientRepository: PatientRepository,
    private val patientLocalStore: AbdmPatientLocalStore,
    private val sessionProvider: AbdmSessionProvider,
    private val config: AbdmConfig,
) : ViewModel() {

    private var pendingSession: AbhaCreateSession? = null
    private var existingPatientUuid: String? = null
    private var existingPatientOpenMrsId: String? = null

    private var mobileTxnId: String? = null

    private val _uiState = MutableStateFlow(AbhaCreateUiState())
    val uiState: StateFlow<AbhaCreateUiState> = _uiState.asStateFlow()

    private val _events = Channel<AbhaCreateEvent>(Channel.RENDEZVOUS)
    val events: Flow<AbhaCreateEvent> = _events.receiveAsFlow()

    fun onInputChanged(field: InputField, value: String) {
        _uiState.update { current ->
            when (field) {
                InputField.Aadhaar -> current.copy(aadhaarNumber = value, aadhaarError = null)
                InputField.AadhaarOtp -> current.copy(aadhaarOtp = value, otpError = null)
                InputField.Mobile -> current.copy(mobileNumber = value, mobileError = null)
                InputField.MobileOtp -> current.copy(mobileOtp = value, otpError = null)
            }
        }
    }


    fun onSendAadhaarOtpClicked() {
        val state = _uiState.value
        if (!state.isConsentChecked) return

        val errorRes = validateAadhaar(state.aadhaarNumber)
        if (errorRes != null) {
            _uiState.update { it.copy(aadhaarError = errorRes) }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(operation = UiState.Loading, loadingMessageRes = R.string.abdm_loading_sending_otp)
            }
            createRepository.requestAadhaarOtp(
                value = state.aadhaarNumber,
                scope = SCOPE_AADHAAR,
            )
                .onSuccess { ack ->
                    _uiState.update {
                        it.copy(
                            operation = UiState.Idle,
                            txnId = ack.txnId,
                            step = AbhaCreateStep.EnterAadhaarOtp,
                            aadhaarLocked = true,
                        )
                    }
                    _events.send(AbhaCreateEvent.ShowSnackbar(message = ack.message, isSuccess = true))
                }
                .onFailure { handleFailure(it) }
        }
    }

    fun onVerifyAadhaarOtpClicked() {
        val state = _uiState.value
        val otpError = validateOtp(state.aadhaarOtp)
        val mobileError = validateMobile(state.mobileNumber)
        if (otpError != null || mobileError != null) {
            _uiState.update { it.copy(otpError = otpError, mobileError = mobileError) }
            return
        }
        val txnId =
            state.txnId ?: return

        viewModelScope.launch {
            _uiState.update {
                it.copy(operation = UiState.Loading, loadingMessageRes = R.string.abdm_loading_verifying_otp)
            }
            createRepository.verifyAadhaarOtp(
                otp = state.aadhaarOtp,
                txnId = txnId,
                mobileNumber = state.mobileNumber,
            )
                .onSuccess { session ->
                    val serverMobile = session.profile.mobile
                    val needsMobileOtp = serverMobile.isBlank() || !serverMobile.equals(
                        state.mobileNumber,
                        ignoreCase = true
                    )

                    if (needsMobileOtp) {
                        _uiState.update {
                            it.copy(
                                step = AbhaCreateStep.EnterMobileOtp,
                                verifiedSession = session,
                                mobileOtp = "",
                                resendAttemptsRemaining = AbhaCreateUiState.MAX_RESEND_ATTEMPTS,
                                resendSecondsRemaining = 0,
                            )
                        }
                        sendMobileOtp(session)
                        startResendCountdown()
                    } else {
                        _uiState.update {
                            it.copy(
                                operation = UiState.Idle,
                                step = AbhaCreateStep.Completed
                            )
                        }
                        routeAfterVerification(session)
                    }
                }
                .onFailure { handleFailure(it) }
        }
    }

    private suspend fun routeAfterVerification(session: AbhaCreateSession) {
        pendingSession = session
        val abhaNumber = session.profile.abhaNumber.replace("-", "")
        val firstAddress = session.profile.phrAddresses.firstOrNull().orEmpty()
        val isAbhaNumberTheOnlyAddress =
            session.profile.phrAddresses.size == 1 &&
                firstAddress.equals("$abhaNumber${config.abhaAddressSuffix}", ignoreCase = true)
        val isNewUser = session.isNew && isAbhaNumberTheOnlyAddress

        if (isNewUser) {
            _events.send(AbhaCreateEvent.NavigateToSuggestions(session))
        } else {
            handleExistingUser(session)
        }
    }

    /** Checks the HMIS server for an existing patient and routes accordingly. */
    private suspend fun handleExistingUser(session: AbhaCreateSession) {
        _uiState.update {
            it.copy(operation = UiState.Loading, loadingMessageRes = R.string.abdm_loading_checking_user)
        }
        patientRepository.checkExistingUser(session.profile.abhaNumber)
            .onSuccess { data ->
                _uiState.update { it.copy(operation = UiState.Idle) }
                if (data.uuid == null) {
                    _events.send(
                        AbhaCreateEvent.CompleteWithResult(
                            session.toAbdmResult(AbdmOutcomes.NAVIGATE_TO_IDENTIFICATION_SCREEN_FOR_NEW_PATIENT_FOR_CREATION),
                        ),
                    )
                } else {
                    if (!data.uuid.equals(NA, ignoreCase = true)) {
                        existingPatientUuid = data.uuid
                        existingPatientOpenMrsId = data.openMrsId
                    }
                    _events.send(
                        AbhaCreateEvent.ShowAddressChecklist(
                            preferredAbhaAddress = session.profile.preferredAbhaAddress.orEmpty(),
                            abhaAddresses = session.profile.phrAddresses,
                        ),
                    )
                }
            }
            .onFailure { handleFailure(it) }
    }

    /** Called when the user picks an existing ABHA address from the checklist. */
    fun onAbhaAddressSelected(abhaAddress: String) {
        val session = pendingSession ?: return
        val reordered = listOf(abhaAddress) +
            session.profile.phrAddresses.filterNot { it == abhaAddress }
        val openMrsId = existingPatientOpenMrsId

        if (openMrsId.isNullOrBlank() || openMrsId.equals(NA, ignoreCase = true)) {
            viewModelScope.launch {
                _events.send(
                    AbhaCreateEvent.CompleteWithResult(
                        session.toAbdmResult(
                            AbdmOutcomes.NAVIGATE_TO_IDENTIFICATION_SCREEN_FOR_NEW_PATIENT_FOR_CREATION,
                            preferredAbhaAddress = abhaAddress,
                            phrAddresses = reordered,
                        ),
                    ),
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(operation = UiState.Loading, loadingMessageRes = R.string.abdm_loading_linking_address)
            }
            val alreadyLinked = patientLocalStore.isPatientLinkedWithAbhaAddress(openMrsId, abhaAddress)
            if (alreadyLinked) {
                completeWithExistingDetails(session, abhaAddress, reordered)
            } else {
                patientRepository.updatePatientIdentifier(
                    patientUuid = existingPatientUuid.orEmpty(),
                    identifier = abhaAddress,
                    identifierType = IDENTIFIER_TYPE_UUID,
                    location = sessionProvider.getLocationUuid(),
                )
                    .onSuccess { completeWithExistingDetails(session, abhaAddress, reordered) }
                    .onFailure { handleFailure(it) }
            }
        }
    }

    /** Called when the user opts to create a new address instead of an existing one. */
    fun onCreateNewAddressRequested() {
        val session = pendingSession ?: return
        viewModelScope.launch {
            _events.send(AbhaCreateEvent.NavigateToSuggestions(session))
        }
    }

    /**
     * Called after the suggestions screen registers a freshly chosen address. For an existing
     * HMIS patient we also link the new address to their record before finishing.
     */
    fun onSuggestionsAddressChosen(chosenAddress: String) {
        val session = pendingSession ?: return
        val reordered = listOf(chosenAddress) +
            session.profile.phrAddresses.filterNot { it == chosenAddress }
        val openMrsId = existingPatientOpenMrsId

        if (openMrsId.isNullOrBlank() || openMrsId.equals(NA, ignoreCase = true)) {
            viewModelScope.launch {
                _events.send(
                    AbhaCreateEvent.CompleteWithResult(
                        session.toAbdmResult(
                            AbdmOutcomes.NAVIGATE_TO_IDENTIFICATION_SCREEN_AFTER_ABHA_SUGGESTIONS_FOR_CREATION,
                            preferredAbhaAddress = chosenAddress,
                            phrAddresses = reordered,
                        ),
                    ),
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(operation = UiState.Loading, loadingMessageRes = R.string.abdm_loading_linking_address)
            }
            patientRepository.updatePatientIdentifier(
                patientUuid = existingPatientUuid.orEmpty(),
                identifier = chosenAddress,
                identifierType = IDENTIFIER_TYPE_UUID,
                location = sessionProvider.getLocationUuid(),
            )
                .onSuccess {
                    existingPatientUuid?.let { uuid ->
                        patientLocalStore.linkAbha(uuid, session.profile.abhaNumber, chosenAddress)
                    }
                    _uiState.update { it.copy(operation = UiState.Idle) }
                    _events.send(
                        AbhaCreateEvent.CompleteWithResult(
                            session.toAbdmResult(
                                AbdmOutcomes.NAVIGATE_TO_IDENTIFICATION_SCREEN_AFTER_ABHA_SUGGESTIONS_FOR_CREATION,
                                preferredAbhaAddress = chosenAddress,
                                phrAddresses = reordered,
                                uuid = existingPatientUuid,
                                openMrsId = existingPatientOpenMrsId,
                            ),
                        ),
                    )
                }
                .onFailure { handleFailure(it) }
        }
    }

    private suspend fun completeWithExistingDetails(
        session: AbhaCreateSession,
        abhaAddress: String,
        reorderedAddresses: List<String>,
    ) {
        existingPatientUuid?.let { uuid ->
            patientLocalStore.linkAbha(uuid, session.profile.abhaNumber, abhaAddress)
        }
        _uiState.update { it.copy(operation = UiState.Idle) }
        _events.send(
            AbhaCreateEvent.CompleteWithResult(
                session.toAbdmResult(
                    AbdmOutcomes.NAVIGATE_TO_IDENTIFICATION_SCREEN_WITH_EXISTING_DETAILS_FOR_CREATION,
                    preferredAbhaAddress = abhaAddress,
                    phrAddresses = reorderedAddresses,
                    uuid = existingPatientUuid,
                    openMrsId = existingPatientOpenMrsId,
                ),
            ),
        )
    }

    fun onSubmitMobileOtpClicked() {
        val state = _uiState.value
        val otpError = validateOtp(state.mobileOtp)
        if (otpError != null) {
            _uiState.update { it.copy(otpError = otpError) }
            return
        }
        val txnId = mobileTxnId ?: state.txnId ?: return

        viewModelScope.launch {
            _uiState.update {
                it.copy(operation = UiState.Loading, loadingMessageRes = R.string.abdm_loading_submitting)
            }
            createRepository.verifyMobileForEnrollment(
                otp = state.mobileOtp,
                txnId = txnId,
                mobileNumber = state.mobileNumber,
            )
                .onSuccess {
                    val base = _uiState.value.verifiedSession ?: return@onSuccess
                    val session = base.copy(profile = base.profile.copy(mobile = state.mobileNumber))
                    _uiState.update {
                        it.copy(
                            operation = UiState.Idle,
                            step = AbhaCreateStep.Completed
                        )
                    }
                    routeAfterVerification(session)
                }
                .onFailure { handleFailure(it) }
        }
    }

    /** Sends an OTP to a newly-entered mobile number (legacy: enrollOTPReq with scope "mobile"). */
    private fun sendMobileOtp(session: AbhaCreateSession) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(operation = UiState.Loading, loadingMessageRes = R.string.abdm_loading_sending_otp)
            }
            createRepository.requestMobileOtp(
                mobileNumber = _uiState.value.mobileNumber,
                txnId = session.txnId,
            )
                .onSuccess { ack ->
                    mobileTxnId = ack.txnId
                    _uiState.update { it.copy(operation = UiState.Idle) }
                    _events.send(AbhaCreateEvent.ShowSnackbar(message = ack.message, isSuccess = true))
                }
                .onFailure { handleFailure(it) }
        }
    }

    fun onResendOtpClicked() {
        val state = _uiState.value
        if (state.resendSecondsRemaining > 0) return

        if (state.resendAttemptsRemaining == 0) {
            _uiState.update { it.copy(operation = UiState.Error(R.string.max_resend_attempts_exceeded)) }
            return
        }

        _uiState.update { it.copy(resendAttemptsRemaining = state.resendAttemptsRemaining - 1) }
        if (state.step == AbhaCreateStep.EnterMobileOtp) {
            val session = state.verifiedSession ?: return
            sendMobileOtp(session)
        } else {
            onSendAadhaarOtpClicked()
        }
        startResendCountdown()
    }

    fun onPatientNameEntered(name: String) {
        _uiState.update { it.copy(patientName = name) }
    }

    fun onConsentChecked(checked: Boolean) {
        _uiState.update { it.copy(isConsentChecked = checked) }
    }

    private var countdownJob: Job? = null

    private fun startResendCountdown(seconds: Int = 60) {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            for (s in seconds downTo 0) {
                _uiState.update { it.copy(resendSecondsRemaining = s) }
                delay(1000)
            }
        }
    }

    private fun handleFailure(error: Throwable) {
        if (error is HttpException && error.httpCode == HTTP_TOO_MANY_REQUESTS) {
            countdownJob?.cancel()
            _uiState.update {
                it.copy(
                    operation = UiState.Error(R.string.abdm_error_otp_rate_limited),
                    resendAttemptsRemaining = 0,
                    resendSecondsRemaining = 0,
                )
            }
            return
        }

        val messageRes = when (error) {
            is OtpVerificationFailedException -> R.string.please_enter_valid_otp
            is HttpException -> when (error.httpCode) {
                400 -> R.string.entered_aadhaar_or_mobile_number_is_incorrect
                422 -> R.string.please_enter_valid_otp
                401, 403 -> R.string.abdm_error_authentication_failed
                in 500..599 -> R.string.abdm_error_server_unavailable
                else -> R.string.abdm_error_generic
            }

            else -> R.string.abdm_error_generic
        }
        val unlockAadhaar = error is HttpException && error.httpCode == 400
        _uiState.update {
            it.copy(
                operation = UiState.Error(messageRes),
                aadhaarLocked = if (unlockAadhaar) false else it.aadhaarLocked,
            )
        }
    }

    private fun validateAadhaar(value: String): Int? = when {
        value.isBlank() -> R.string.abdm_error_aadhaar_required
        value.length != 12 || !value.all { it.isDigit() } -> R.string.abdm_error_aadhaar_invalid
        !VerhoeffAlgorithm.isValid(value) -> R.string.abdm_error_aadhaar_invalid
        else -> null
    }

    private fun validateMobile(value: String): Int? = when {
        value.isBlank() -> R.string.abdm_error_mobile_required
        value.length != 10 || !value.all { it.isDigit() } -> R.string.abdm_error_mobile_invalid
        else -> null
    }

    private fun validateOtp(value: String): Int? = when {
        value.isBlank() -> R.string.abdm_error_otp_required
        value.length != 6 || !value.all { it.isDigit() } -> R.string.abdm_error_otp_invalid
        else -> null
    }

    private companion object {
        const val SCOPE_AADHAAR = "aadhar"
        const val HTTP_TOO_MANY_REQUESTS = 429
        const val NA = "NA"
        const val IDENTIFIER_TYPE_UUID = "59077d8f-8bee-4a6f-a1a8-64365a297da6"
    }
}