package org.intelehealth.abdm.presentation.abha_suggestions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.intelehealth.abdm.R
import org.intelehealth.abdm.config.AbdmConfig
import org.intelehealth.abdm.config.AbdmSessionProvider
import org.intelehealth.abdm.data.remote.extensions.HttpException
import org.intelehealth.abdm.domain.repository.AbhaSuggestionsRepository
import org.intelehealth.abdm.domain.repository.PatientRepository
import org.intelehealth.abdm.presentation.common.UiState
import javax.inject.Inject

@HiltViewModel
internal class AbhaSuggestionsViewModel @Inject constructor(
    private val repository: AbhaSuggestionsRepository,
    private val patientRepository: PatientRepository,
    private val sessionProvider: AbdmSessionProvider,
    private val config: AbdmConfig,
) : ViewModel() {

    /** The active environment's ABHA suffix (e.g. "@sbx"/"@abdm"); shown next to the input. */
    val abhaAddressSuffix: String get() = config.abhaAddressSuffix

    private val _uiState = MutableStateFlow(AbhaSuggestionsUiState())
    val uiState: StateFlow<AbhaSuggestionsUiState> = _uiState.asStateFlow()

    private val _events = Channel<AbhaSuggestionsEvent>(Channel.RENDEZVOUS)
    val events: Flow<AbhaSuggestionsEvent> = _events.receiveAsFlow()

    private var hasLoaded = false

    fun loadSuggestions(txnId: String) {
        if (hasLoaded) return
        hasLoaded = true
        viewModelScope.launch {
            _uiState.update {
                it.copy(operation = UiState.Loading, loadingMessageRes = R.string.abdm_loading_fetching_suggestions)
            }
            repository.fetchSuggestions(txnId)
                .onSuccess { suggestions ->
                    _uiState.update {
                        it.copy(operation = UiState.Idle, addresses = suggestions.addresses)
                    }
                }
                .onFailure { error -> _uiState.update { it.copy(operation = UiState.Error(mapError(error))) } }
        }
    }

    fun onAddressChanged() {
        if (_uiState.value.addressError != null) {
            _uiState.update { it.copy(addressError = null) }
        }
    }

    /**
     * [patientUuid] is empty when the module's own create flow drives this screen — that flow links
     * the address itself in AbhaCreateViewModel, so linking here too would double-call. It is
     * supplied only when the host re-enters from registration to add a further address, where
     * nothing else performs the link.
     */
    fun onSubmitClicked(
        txnId: String,
        abhaAddress: String,
        defaultAddress: String,
        patientUuid: String = "",
    ) {
        if (defaultAddress.isNotBlank() && abhaAddress.equals(defaultAddress, ignoreCase = true)) {
            viewModelScope.launch {
                _events.send(AbhaSuggestionsEvent.AddressRegistered(abhaAddress))
            }
            return
        }
        val errorRes = validateAbhaAddress(abhaAddress)
        if (errorRes != null) {
            _uiState.update { it.copy(addressError = errorRes) }
            return
        }
        viewModelScope.launch {
            _uiState.update {
                it.copy(operation = UiState.Loading, loadingMessageRes = R.string.abdm_loading_saving_address)
            }
            repository.registerPreferredAddress(txnId, abhaAddress)
                .onSuccess { registered ->
                    val address = withSuffix(registered.preferredAbhaAddress.ifBlank { abhaAddress })
                    if (patientUuid.isBlank()) {
                        _uiState.update { it.copy(operation = UiState.Idle) }
                        _events.send(AbhaSuggestionsEvent.AddressRegistered(address))
                    } else {
                        linkThenFinish(patientUuid, address)
                    }
                }
                .onFailure { error -> _uiState.update { it.copy(operation = UiState.Error(mapError(error))) } }
        }
    }

    /**
     * The address is already registered with ABDM by this point, so a failure here leaves real work
     * done but unlinked. Since this screen suppresses the back press, the failure also sets
     * [AbhaSuggestionsUiState.allowExit] so the user is not trapped with no way out.
     */
    private suspend fun linkThenFinish(patientUuid: String, abhaAddress: String) {
        _uiState.update {
            it.copy(operation = UiState.Loading, loadingMessageRes = R.string.abdm_loading_linking_address)
        }
        patientRepository.updatePatientIdentifier(
            patientUuid = patientUuid,
            identifier = abhaAddress,
            identifierType = IDENTIFIER_TYPE_UUID,
            location = sessionProvider.getLocationUuid(),
        )
            .onSuccess {
                _uiState.update { it.copy(operation = UiState.Idle) }
                _events.send(AbhaSuggestionsEvent.AddressRegistered(abhaAddress))
            }
            .onFailure {
                _uiState.update {
                    it.copy(
                        operation = UiState.Error(R.string.abdm_error_abha_address_link_failed),
                        allowExit = true,
                    )
                }
            }
    }

    /** The server may return the address without its environment suffix. */
    private fun withSuffix(abhaAddress: String): String =
        if (abhaAddress.endsWith(config.abhaAddressSuffix, ignoreCase = true)) abhaAddress
        else "$abhaAddress${config.abhaAddressSuffix}"

    private fun validateAbhaAddress(text: String): Int? = when {
        text.isBlank() -> R.string.please_select_abha_address
        text.contains(config.abhaAddressSuffix, ignoreCase = true) -> R.string.abha_address_suffix_not_allowed_validation
        text.length < MIN_LENGTH || text.length > MAX_LENGTH -> R.string.length_validation
        !text.matches(ALLOWED_CHARS) -> R.string.characters_validation
        text.startsWith(".") || text.startsWith("_") ||
            text.endsWith(".") || text.endsWith("_") -> R.string.special_characters_position_validation
        !specialCharsWithinLimit(text) -> R.string.special_characters_count_validation
        else -> null
    }

    private fun specialCharsWithinLimit(text: String): Boolean =
        text.count { it == '.' } <= 1 && text.count { it == '_' } <= 1

    private fun mapError(error: Throwable): Int = when (error) {
        is HttpException -> when (error.httpCode) {
            HTTP_CONFLICT -> R.string.abdm_error_abha_address_exists
            in 500..599 -> R.string.abdm_error_server_unavailable
            else -> R.string.abdm_error_generic
        }

        else -> R.string.abdm_error_generic
    }

    private companion object {
        const val MIN_LENGTH = 8
        const val MAX_LENGTH = 18
        const val HTTP_CONFLICT = 409

        /** Same OpenMRS PatientIdentifierType as AbhaCreateViewModel and AbhaVerifyViewModel use. */
        const val IDENTIFIER_TYPE_UUID = "59077d8f-8bee-4a6f-a1a8-64365a297da6"
        val ALLOWED_CHARS = Regex("^[A-Za-z0-9._]+$")
    }
}
