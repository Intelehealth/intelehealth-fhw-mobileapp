package org.intelehealth.abdm.presentation.abha_verify

import androidx.annotation.StringRes
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
import org.intelehealth.abdm.config.LocalPatientRecord
import org.intelehealth.abdm.data.remote.extensions.HttpException
import org.intelehealth.abdm.domain.model.AbhaProfile
import org.intelehealth.abdm.domain.model.AbhaSearchResult
import org.intelehealth.abdm.domain.model.AbhaVerifySession
import org.intelehealth.abdm.domain.repository.AbhaProfileRepository
import org.intelehealth.abdm.domain.repository.AbhaVerifyRepository
import org.intelehealth.abdm.domain.repository.PatientRepository
import org.intelehealth.abdm.presentation.common.UiState
import org.intelehealth.abdm.util.VerhoeffAlgorithm
import javax.inject.Inject

/**
 * Drives the "verify an existing ABHA" flow across three identifier methods (Aadhaar / Mobile /
 * ABHA number-or-address). Tokens are handled transparently by the network layer, so unlike legacy
 * there is no manual getToken/Bearer plumbing here. The two pick-one dialogs (OTP channel, ABHA
 * account) are surfaced as events and answered via [onAuthTypeSelected] / [onAccountSelected].
 */
@HiltViewModel
internal class AbhaVerifyViewModel @Inject constructor(
    private val verifyRepository: AbhaVerifyRepository,
    private val profileRepository: AbhaProfileRepository,
    private val patientRepository: PatientRepository,
    private val patientLocalStore: AbdmPatientLocalStore,
    private val sessionProvider: AbdmSessionProvider,
    private val config: AbdmConfig,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AbhaVerifyUiState())
    val uiState: StateFlow<AbhaVerifyUiState> = _uiState.asStateFlow()

    private val _events = Channel<AbhaVerifyEvent>(Channel.RENDEZVOUS)
    val events: Flow<AbhaVerifyEvent> = _events.receiveAsFlow()

    /** The active environment's ABHA suffix (e.g. "@sbx"/"@abdm"); used in the address hint. */
    val abhaAddressSuffix: String get() = config.abhaAddressSuffix

    private var xToken: String? = null
    private var searchTxnId: String? = null
    private var lastSend: SendParams? = null
    private var accountStage: AccountStage = AccountStage.None
    private var pendingProfileScope: String? = null
    private var pendingTxnId: String? = null
    private var pendingSendValue: String? = null
    private var pendingSendScope: String? = null

    private var countdownJob: Job? = null

    fun seedPatientName(name: String) {
        _uiState.update { it.copy(patientName = name) }
    }

    fun onMethodSelected(method: AbhaVerifyMethod) {
        countdownJob?.cancel()
        _uiState.update {
            it.copy(
                method = method,
                step = AbhaVerifyStep.EnterDetails,
                otp = "",
                txnId = null,
                aadhaarError = null,
                mobileError = null,
                abhaError = null,
                otpError = null,
                resendSecondsRemaining = 0,
                resendAttemptsRemaining = AbhaVerifyUiState.MAX_RESEND_ATTEMPTS,
            )
        }
    }

    fun onConsentChecked(checked: Boolean) {
        _uiState.update { it.copy(isConsentChecked = checked) }
    }

    fun onInputChanged(field: AbhaVerifyInputField, value: String) {
        _uiState.update {
            when (field) {
                AbhaVerifyInputField.Aadhaar -> it.copy(aadhaarNumber = value, aadhaarError = null)
                AbhaVerifyInputField.Mobile -> it.copy(mobileNumber = value, mobileError = null)
                AbhaVerifyInputField.AbhaNumber -> it.copy(abhaNumber = value, abhaError = null)
                AbhaVerifyInputField.AbhaAddress -> it.copy(abhaAddress = value, abhaError = null)
                AbhaVerifyInputField.Otp -> it.copy(otp = value, otpError = null)
            }
        }
    }

    /** "Send OTP" / "Search Profiles" / "Verify" — the single primary button. */
    fun onPrimaryClicked() {
        val state = _uiState.value
        when (state.step) {
            AbhaVerifyStep.EnterDetails -> startVerification(state)
            AbhaVerifyStep.EnterOtp -> verifyOtp(state)
        }
    }

    private fun startVerification(state: AbhaVerifyUiState) {
        _uiState.update {
            it.copy(
                resendAttemptsRemaining = AbhaVerifyUiState.MAX_RESEND_ATTEMPTS,
                resendSecondsRemaining = 0,
            )
        }
        when (state.method) {
            AbhaVerifyMethod.Aadhaar -> {
                if (!state.isConsentChecked) return
                val error = validateAadhaar(state.aadhaarNumber)
                if (error != null) {
                    _uiState.update { it.copy(aadhaarError = error) }
                    return
                }
                sendOtp(state.aadhaarNumber.trim(), SCOPE_AADHAAR, authMethod = null, txnId = null)
            }

            AbhaVerifyMethod.Mobile -> {
                val error = validateMobile(state.mobileNumber)
                if (error != null) {
                    _uiState.update { it.copy(mobileError = error) }
                    return
                }
                searchProfiles(state.mobileNumber.trim())
            }

            AbhaVerifyMethod.Abha -> startAbhaVerification(state)
        }
    }

    private fun startAbhaVerification(state: AbhaVerifyUiState) {
        val abhaNumber = state.abhaNumber.trim()
        val abhaAddress = state.abhaAddress.trim()

        if (abhaNumber.isBlank() && abhaAddress.isBlank()) {
            _uiState.update { it.copy(abhaError = R.string.abdm_error_abha_required) }
            return
        }

        if (abhaNumber.isNotBlank()) {
            if (abhaNumber.filter { it.isDigit() }.length < ABHA_NUMBER_DIGITS) {
                _uiState.update { it.copy(abhaError = R.string.abdm_error_abha_number_invalid) }
                return
            }
            pendingSendValue = formatAbhaNumber(abhaNumber)
            pendingSendScope = SCOPE_ABHA_NUMBER
            viewModelScope.launch {
                _events.send(
                    AbhaVerifyEvent.ShowAuthTypeSelection(listOf(ABHA_OTP_AADHAAR, ABHA_OTP_MOBILE)),
                )
            }
            return
        }

        if (!abhaAddress.endsWith(config.abhaAddressSuffix, ignoreCase = true)) {
            _uiState.update { it.copy(abhaError = R.string.abdm_error_abha_address_invalid) }
            return
        }
        fetchAuthModes(abhaAddress)
    }

    private fun fetchAuthModes(abhaAddress: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(operation = UiState.Loading, loadingMessageRes = R.string.abdm_loading_default)
            }
            verifyRepository.fetchAuthModes(xToken = "", abhaAddress = abhaAddress)
                .onSuccess { modes ->
                    _uiState.update { it.copy(operation = UiState.Idle) }
                    pendingSendValue = abhaAddress
                    pendingSendScope = SCOPE_ABHA_ADDRESS
                    val methods = modes.authMethods.ifEmpty { listOf(ABHA_OTP_AADHAAR, ABHA_OTP_MOBILE) }
                    _events.send(AbhaVerifyEvent.ShowAuthTypeSelection(methods))
                }
                .onFailure { handleFailure(it) }
        }
    }

    /** User picked an OTP channel for the ABHA path. */
    fun onAuthTypeSelected(authMethod: String) {
        val value = pendingSendValue ?: return
        val scope = pendingSendScope ?: return
        sendOtp(value, scope, authMethod = authMethod, txnId = null)
    }

    private fun searchProfiles(mobile: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(operation = UiState.Loading, loadingMessageRes = R.string.abdm_loading_fetching_profiles)
            }
            verifyRepository.fetchAbhaProfiles(mobile)
                .onSuccess { searched ->
                    _uiState.update { it.copy(operation = UiState.Idle) }
                    searchTxnId = searched.txnId
                    accountStage = AccountStage.MobileSearch
                    _events.send(
                        AbhaVerifyEvent.ShowAccountSelection(
                            searched.abhaSearchResults.map { it.toChoice() },
                        ),
                    )
                }
                .onFailure { error ->
                    if (error.is404()) {
                        _uiState.update { it.copy(operation = UiState.Idle) }
                        _events.send(AbhaVerifyEvent.PromptCreateAbha(R.string.abdm_error_no_abha_records))
                    } else {
                        handleFailure(error)
                    }
                }
        }
    }

    /** User picked one of several ABHA accounts (mobile search, or post-verify multi-account). */
    fun onAccountSelected(choice: AccountChoice) {
        when (accountStage) {
            AccountStage.MobileSearch -> {
                accountStage = AccountStage.None
                val index = choice.index ?: return
                sendOtp(index.toString(), SCOPE_INDEX, authMethod = ABHA_OTP_MOBILE, txnId = searchTxnId)
            }

            AccountStage.VerifyAccounts -> {
                accountStage = AccountStage.None
                fetchProfile(choice.abhaNumber, pendingTxnId, xToken, pendingProfileScope)
            }

            AccountStage.None -> Unit
        }
    }

    private fun sendOtp(value: String, scope: String, authMethod: String?, txnId: String?) {
        lastSend = SendParams(value, scope, authMethod, txnId)
        viewModelScope.launch {
            _uiState.update {
                it.copy(operation = UiState.Loading, loadingMessageRes = R.string.abdm_loading_sending_otp)
            }
            verifyRepository.requestMobileOtp(value, scope, authMethod, txnId)
                .onSuccess { ack ->
                    _uiState.update {
                        it.copy(
                            operation = UiState.Idle,
                            step = AbhaVerifyStep.EnterOtp,
                            txnId = ack.txnId,
                            otp = "",
                            otpError = null,
                        )
                    }
                    startResendCountdown()
                    _events.send(AbhaVerifyEvent.ShowSnackbar(message = ack.message, isSuccess = true))
                }
                .onFailure { error ->
                    if (error.is404()) {
                        _uiState.update { it.copy(operation = UiState.Idle) }
                        _events.send(
                            AbhaVerifyEvent.ShowSnackbar(messageRes = loginOtpNotFoundMessageRes(), isSuccess = false),
                        )
                    } else {
                        handleFailure(error)
                    }
                }
        }
    }

    @StringRes
    private fun loginOtpNotFoundMessageRes(): Int = when (_uiState.value.method) {
        AbhaVerifyMethod.Aadhaar -> R.string.abdm_error_invalid_aadhaar
        AbhaVerifyMethod.Mobile -> R.string.abdm_error_mobile_no_match
        AbhaVerifyMethod.Abha -> R.string.abdm_error_invalid_abha
    }

    private fun verifyOtp(state: AbhaVerifyUiState) {
        val otpError = validateOtp(state.otp)
        if (otpError != null) {
            _uiState.update { it.copy(otpError = otpError) }
            return
        }
        val txnId = state.txnId ?: return
        val scope = when (state.method) {
            AbhaVerifyMethod.Aadhaar -> SCOPE_AADHAAR
            AbhaVerifyMethod.Mobile -> SCOPE_MOBILE
            AbhaVerifyMethod.Abha -> lastSend?.scope ?: return
        }
        val authMethod = if (state.method == AbhaVerifyMethod.Abha) lastSend?.authMethod else null

        viewModelScope.launch {
            _uiState.update {
                it.copy(operation = UiState.Loading, loadingMessageRes = R.string.abdm_loading_verifying_otp)
            }
            verifyRepository.verifyLoginOtp(state.otp, txnId, scope, authMethod)
                .onSuccess { session -> handleVerified(session, scope, state.method, txnId) }
                .onFailure { error ->
                    if (state.method == AbhaVerifyMethod.Aadhaar && error.is404()) {
                        _uiState.update { it.copy(operation = UiState.Idle) }
                        _events.send(AbhaVerifyEvent.PromptCreateAbha(R.string.abdm_error_no_abha_records_aadhaar))
                    } else {
                        handleFailure(error)
                    }
                }
        }
    }

    private suspend fun handleVerified(
        session: AbhaVerifySession,
        scope: String,
        method: AbhaVerifyMethod,
        requestTxnId: String,
    ) {
        val effectiveTxnId = session.txnId.ifBlank { requestTxnId }
        if (session.authResult.equals(AUTH_RESULT_FAILED, ignoreCase = true)) {
            _uiState.update { it.copy(operation = UiState.Idle) }
            _events.send(
                AbhaVerifyEvent.ShowSnackbar(
                    message = session.message,
                    messageRes = R.string.please_enter_valid_otp,
                    isSuccess = false,
                ),
            )
            return
        }
        xToken = BEARER + session.token

        if (scope == SCOPE_ABHA_ADDRESS) {
            if (session.user?.kycStatus.equals(KYC_STATUS_PENDING, ignoreCase = true)) {
                _uiState.update { it.copy(operation = UiState.Idle, otp = "") }
                _events.send(AbhaVerifyEvent.ShowSnackbar(messageRes = R.string.abdm_error_kyc_pending, isSuccess = false))
                return
            }
            fetchProfile(abhaNumber = null, txnId = effectiveTxnId, xToken = xToken, profileScope = SCOPE_ABHA_ADDRESS)
            return
        }

        val accounts = session.accounts
        if (accounts.isEmpty()) {
            _uiState.update { it.copy(operation = UiState.Idle) }
            _events.send(AbhaVerifyEvent.ShowSnackbar(messageRes = R.string.abdm_error_generic, isSuccess = false))
            return
        }
        val profileScope = if (method == AbhaVerifyMethod.Aadhaar) SCOPE_AADHAAR else SCOPE_ABHA_NUMBER

        if (method == AbhaVerifyMethod.Aadhaar || accounts.size == 1) {
            fetchProfile(accounts.first().abhaNumber, effectiveTxnId, xToken, profileScope)
        } else {
            accountStage = AccountStage.VerifyAccounts
            pendingProfileScope = profileScope
            pendingTxnId = effectiveTxnId
            _uiState.update { it.copy(operation = UiState.Idle) }
            _events.send(
                AbhaVerifyEvent.ShowAccountSelection(
                    accounts.map { it.toChoice() },
                ),
            )
        }
    }

    private fun fetchProfile(
        abhaNumber: String?,
        txnId: String?,
        xToken: String?,
        profileScope: String?,
    ) {
        val resolvedTxnId = txnId ?: return
        val resolvedXToken = xToken ?: return
        val resolvedScope = profileScope ?: return
        viewModelScope.launch {
            _uiState.update {
                it.copy(operation = UiState.Loading, loadingMessageRes = R.string.abdm_loading_fetching_profile)
            }
            profileRepository.fetchAbhaProfile(resolvedXToken, resolvedTxnId, abhaNumber, resolvedScope)
                .onSuccess { profile -> routeAfterProfile(profile, resolvedXToken, resolvedTxnId) }
                .onFailure { handleFailure(it) }
        }
    }

    private suspend fun routeAfterProfile(profile: AbhaProfile, xToken: String, txnId: String) {
        _uiState.update {
            it.copy(operation = UiState.Loading, loadingMessageRes = R.string.abdm_loading_checking_user)
        }
        patientRepository.checkExistingUser(profile.abhaNumber)
            .onSuccess { serverData ->
                val uuid = serverData.uuid
                if (uuid == null || uuid.equals(NA, ignoreCase = true)) {
                    _uiState.update { it.copy(operation = UiState.Idle) }
                    _events.send(
                        AbhaVerifyEvent.CompleteWithResult(profile.toNewPatientVerifyResult(xToken, txnId)),
                    )
                    return@onSuccess
                }
                val local = patientLocalStore.findPatientForComparison(profile.abhaNumber, profile.mobile)
                if (local == null) {
                    _uiState.update { it.copy(operation = UiState.Idle) }
                    _events.send(AbhaVerifyEvent.ShowSnackbar(messageRes = R.string.abdm_error_generic, isSuccess = false))
                    return@onSuccess
                }
                linkThenCompare(profile, local, xToken, txnId)
            }
            .onFailure { handleFailure(it) }
    }

    /**
     * Links the chosen ABHA address to the existing patient's server identifier (unless already
     * linked), then routes to compare. Mirrors legacy: the address is the one typed on the
     * ABHA-address path, otherwise the profile's preferred address.
     */
    private suspend fun linkThenCompare(
        profile: AbhaProfile,
        local: LocalPatientRecord,
        xToken: String,
        txnId: String,
    ) {
        val enteredAbhaAddress = _uiState.value.abhaAddress.trim()
        val abhaAddressToLink = enteredAbhaAddress.ifBlank { profile.preferredAbhaAddress }
        val alreadyLinked = abhaAddressToLink.isNotBlank() &&
            local.abhaAddress.contains(abhaAddressToLink, ignoreCase = true)

        if (abhaAddressToLink.isBlank() || alreadyLinked) {
            _uiState.update { it.copy(operation = UiState.Idle) }
            sendToCompare(profile, local, xToken, txnId)
            return
        }

        _uiState.update {
            it.copy(operation = UiState.Loading, loadingMessageRes = R.string.abdm_loading_linking_address)
        }
        patientRepository.updatePatientIdentifier(
            patientUuid = local.uuid,
            identifier = abhaAddressToLink,
            identifierType = IDENTIFIER_TYPE_UUID,
            location = sessionProvider.getLocationUuid(),
        )
        _uiState.update { it.copy(operation = UiState.Idle) }
        sendToCompare(profile, local, xToken, txnId)
    }

    private suspend fun sendToCompare(
        profile: AbhaProfile,
        local: LocalPatientRecord,
        xToken: String,
        txnId: String,
    ) {
        _events.send(
            AbhaVerifyEvent.NavigateToCompare(
                localRecord = local,
                abhaRecord = profile.toComparisonRecord(local.uuid, local.openMrsId),
                xToken = xToken,
                txnId = txnId,
            ),
        )
    }

    fun onResendOtpClicked() {
        val state = _uiState.value
        if (state.resendSecondsRemaining > 0) return
        if (state.resendAttemptsRemaining == 0) {
            viewModelScope.launch {
                _events.send(AbhaVerifyEvent.ShowSnackbar(messageRes = R.string.max_resend_attempts_exceeded, isSuccess = false))
            }
            return
        }
        val send = lastSend ?: return
        _uiState.update { it.copy(resendAttemptsRemaining = state.resendAttemptsRemaining - 1) }
        sendOtp(send.value, send.scope, send.authMethod, send.txnId)
    }

    private fun startResendCountdown(seconds: Int = 60) {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            for (s in seconds downTo 0) {
                _uiState.update { it.copy(resendSecondsRemaining = s) }
                delay(1000)
            }
        }
    }

    private suspend fun handleFailure(error: Throwable) {
        val serverMessage = (error as? HttpException)?.serverMessage
        val fallbackRes = when (error) {
            is HttpException -> when (error.httpCode) {
                422 -> R.string.please_enter_valid_otp
                401, 403 -> R.string.abdm_error_authentication_failed
                in 500..599 -> R.string.abdm_error_server_unavailable
                else -> R.string.abdm_error_generic
            }

            else -> R.string.abdm_error_generic
        }
        _uiState.update { it.copy(operation = UiState.Idle) }
        _events.send(
            AbhaVerifyEvent.ShowSnackbar(
                message = serverMessage,
                messageRes = fallbackRes,
                isSuccess = false,
            ),
        )
    }

    /**
     * Projects an ABHA account into a picker row, resolving its local-registration status via the
     * bridge (legacy: abha_number LIKE %last4% AND first/last name). Call from a coroutine.
     */
    private suspend fun AbhaSearchResult.toChoice(): AccountChoice {
        val parts = name.trim().split(" ").filter { it.isNotBlank() }
        val firstName = parts.getOrElse(0) { "" }
        val lastName = parts.getOrElse(1) { "" }
        val lastFour = abhaNumber.filter { it.isDigit() }.takeLast(4)
        val registered = firstName.isNotEmpty() && lastFour.isNotEmpty() &&
            patientLocalStore.isPatientRegisteredLocally(lastFour, firstName, lastName)
        return AccountChoice(
            name = name,
            abhaNumber = abhaNumber,
            gender = gender,
            index = index,
            isRegisteredLocally = registered,
        )
    }

    /**
     * Formats a 14-digit ABHA number into the dashed form the server expects ("16-8810-8867-0877",
     * groups of 2-4-4-4). Ports legacy AbdmUtils.formatIntoAbhaString. Returns the input unchanged
     * if it isn't exactly 14 digits (validation already guards this).
     */
    private fun formatAbhaNumber(input: String): String {
        val digits = input.filter { it.isDigit() }
        if (digits.length != ABHA_NUMBER_DIGITS) return input
        return "${digits.substring(0, 2)}-${digits.substring(2, 6)}-" +
            "${digits.substring(6, 10)}-${digits.substring(10, 14)}"
    }

    private fun Throwable.is404(): Boolean = this is HttpException && httpCode == HTTP_NOT_FOUND

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

    private data class SendParams(
        val value: String,
        val scope: String,
        val authMethod: String?,
        val txnId: String?,
    )

    private enum class AccountStage { None, MobileSearch, VerifyAccounts }

    private companion object {
        const val SCOPE_AADHAAR = "aadhar"
        const val SCOPE_MOBILE = "mobile"
        const val SCOPE_INDEX = "index"
        const val SCOPE_ABHA_ADDRESS = "abha-address"
        const val SCOPE_ABHA_NUMBER = "abha-number"
        const val ABHA_OTP_AADHAAR = "AADHAAR_OTP"
        const val ABHA_OTP_MOBILE = "MOBILE_OTP"
        const val KYC_STATUS_PENDING = "PENDING"
        const val AUTH_RESULT_FAILED = "failed"
        const val NA = "NA"
        const val BEARER = "Bearer "
        const val ABHA_NUMBER_DIGITS = 14
        const val HTTP_NOT_FOUND = 404
        const val IDENTIFIER_TYPE_UUID = "59077d8f-8bee-4a6f-a1a8-64365a297da6"
    }
}
