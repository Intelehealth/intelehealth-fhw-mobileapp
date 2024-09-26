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
import org.intelehealth.abdm.common.utils.ErrorCode.CODE_429
import org.intelehealth.abdm.common.utils.PreferenceUtils
import org.intelehealth.abdm.domain.model.AuthToken
import org.intelehealth.abdm.domain.result.ApiResult
import org.intelehealth.abdm.domain.usecase.GetAuthTokenUseCase
import org.intelehealth.abdm.features.base.BaseViewModel
import org.intelehealth.abdm.features.viewstate.GetAuthTokenViewState
import org.intelehealth.abdm.features.viewstate.SendAadhaarOtpViewState
import org.intelehealth.abdm.features.viewstate.VerifyAadhaarOtpViewState
import javax.inject.Inject

@HiltViewModel
class AbdmRegistrationViewModel @Inject constructor(
    private val getAuthTokenUseCase: GetAuthTokenUseCase,
    @IoDispatcher
    private val ioDispatcher: CoroutineDispatcher,
    @ApplicationContext private val appContext: Context,
) : BaseViewModel() {


    private val _getAuthTokenState =
        MutableLiveData<GetAuthTokenViewState>(GetAuthTokenViewState.Idle)
    val authTokenState: LiveData<GetAuthTokenViewState>
        get() = _getAuthTokenState


    init {
        getAuthToken()
    }


    private fun getAuthToken() {
        viewModelScope.launch(ioDispatcher) {
            val authApiResult = getAuthTokenUseCase()
            if (authApiResult is ApiResult.Success) {
                _getAuthTokenState.postValue(
                    GetAuthTokenViewState.Success(authApiResult.data)
                )
            } else {
                _getAuthTokenState.postValue(
                    GetAuthTokenViewState.Error(
                        appContext.getString(
                            R.string.something_went_wrong
                        )
                    )
                )
            }

        }
    }

}