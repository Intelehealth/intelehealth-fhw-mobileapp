package org.intelehealth.abdm.presentation.abha_verify

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
import org.intelehealth.abdm.config.AbdmPatientLocalStore
import org.intelehealth.abdm.config.LocalPatientRecord
import org.intelehealth.abdm.presentation.common.UiState
import org.intelehealth.abdm.result.AbdmResult
import javax.inject.Inject

@HiltViewModel
internal class AbhaCompareViewModel @Inject constructor(
    private val patientLocalStore: AbdmPatientLocalStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState.Idle as UiState<Unit>)
    val uiState: StateFlow<UiState<Unit>> = _uiState.asStateFlow()

    private val _events = Channel<AbdmResult>(Channel.RENDEZVOUS)
    val events: Flow<AbdmResult> = _events.receiveAsFlow()

    /** Persists the ABHA-side record onto the existing local patient row, then returns the result. */
    fun save(abhaRecord: LocalPatientRecord, xToken: String, txnId: String) {
        viewModelScope.launch {
            _uiState.update { UiState.Loading }
            val saved = patientLocalStore.savePatientAfterComparison(abhaRecord)
            if (saved) {
                _uiState.update { UiState.Idle }
                _events.send(abhaRecord.toAfterComparisonResult(xToken, txnId))
            } else {
                _uiState.update { UiState.Error(R.string.abdm_compare_save_failed) }
            }
        }
    }
}
