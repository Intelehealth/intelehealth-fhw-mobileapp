package org.intelehealth.abdm.features.viewmodel.registration

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import org.intelehealth.abdm.R
import org.intelehealth.abdm.common.di.IoDispatcher
import org.intelehealth.abdm.common.utils.PreferenceUtils
import org.intelehealth.abdm.domain.model.request.AbhaAddressSuggestionRequest
import org.intelehealth.abdm.domain.model.request.EnrollAbhaAddressRequest
import org.intelehealth.abdm.domain.result.ApiResult
import org.intelehealth.abdm.domain.usecase.registration.AbhaSuggestionListUseCase
import org.intelehealth.abdm.domain.usecase.registration.EnrollAbhaAddressUseCase
import org.intelehealth.abdm.features.base.BaseViewModel
import org.intelehealth.abdm.features.intent.EnrollAbhaAddressIntent
import org.intelehealth.abdm.features.viewstate.AbhaAddressSuggestionViewState
import org.intelehealth.abdm.features.viewstate.EnrollAbhaAddressViewState
import javax.inject.Inject

@HiltViewModel
class SelectAbhaAddressViewModel @Inject constructor(
    private val abhaSuggestionListUseCase: AbhaSuggestionListUseCase,
    private val enrollAbhaAddressUseCase: EnrollAbhaAddressUseCase,
    @IoDispatcher
    private val ioDispatcher: CoroutineDispatcher,
    @ApplicationContext private val appContext: Context,
) : BaseViewModel() {


    private val _abhaAddressSuggestionState =
        MutableLiveData<AbhaAddressSuggestionViewState>(AbhaAddressSuggestionViewState.Idle)
    val abhaAddressSuggestionState: LiveData<AbhaAddressSuggestionViewState>
        get() = _abhaAddressSuggestionState

    private val _enrollAbhaAddressState =
        MutableLiveData<EnrollAbhaAddressViewState>(EnrollAbhaAddressViewState.Idle)
    val enrollAbhaAddressState: LiveData<EnrollAbhaAddressViewState>
        get() = _enrollAbhaAddressState

    fun sendIntent(intent: EnrollAbhaAddressIntent) {
        when (intent) {
            is EnrollAbhaAddressIntent.GetSuggestionList ->
                getAbhaSuggestionList(
                    PreferenceUtils.getAuthToken(appContext) ?: "",
                    AbhaAddressSuggestionRequest(intent.txnId)
                )

            is EnrollAbhaAddressIntent.EnrollAbhaAddress ->
                enrollAbhaAddress(
                    PreferenceUtils.getAuthToken(appContext) ?: "",
                    EnrollAbhaAddressRequest(intent.txnId, intent.abhaAddress)
                )
        }
    }


    private fun getAbhaSuggestionList(
        authHeader: String,
        apiRequest: AbhaAddressSuggestionRequest,
    ) {

        viewModelScope.launch(ioDispatcher) {
            _abhaAddressSuggestionState.postValue(AbhaAddressSuggestionViewState.Loading)
            when (val apiResult = abhaSuggestionListUseCase(authHeader, apiRequest)) {

                is ApiResult.Success -> {
                    _abhaAddressSuggestionState.postValue(
                        AbhaAddressSuggestionViewState.Success(
                            apiResult.data
                        )
                    )
                }

                is ApiResult.Error -> {
                    _abhaAddressSuggestionState.postValue(
                        AbhaAddressSuggestionViewState.Error(
                            appContext.getString(
                                R.string.something_went_wrong
                            )
                        )
                    )
                }
            }
        }
    }


    private fun enrollAbhaAddress(
        authHeader: String,
        apiRequest: EnrollAbhaAddressRequest,
    ) {

        viewModelScope.launch(ioDispatcher) {
            _enrollAbhaAddressState.postValue(EnrollAbhaAddressViewState.Loading)
            when (val apiResult = enrollAbhaAddressUseCase(authHeader, apiRequest)) {

                is ApiResult.Success -> {
                    _enrollAbhaAddressState.postValue(
                        EnrollAbhaAddressViewState.Success(
                            apiResult.data
                        )
                    )
                }

                is ApiResult.Error -> {
                    _enrollAbhaAddressState.postValue(
                        EnrollAbhaAddressViewState.Error(
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