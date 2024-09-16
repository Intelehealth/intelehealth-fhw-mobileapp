package org.intelehealth.abdm.features.viewmodel.registration

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import org.intelehealth.abdm.common.di.IoDispatcher
import org.intelehealth.abdm.domain.model.RegistrationConsent
import org.intelehealth.abdm.domain.usecase.registration.AbhaRegistrationConsentUseCase
import org.intelehealth.abdm.features.base.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class AbhaRegistrationConsentViewModel @Inject constructor(
    private val consentUseCase: AbhaRegistrationConsentUseCase,
    @IoDispatcher
    private val ioDispatcher: CoroutineDispatcher
) : BaseViewModel() {

    private val _consentLiveData = MutableLiveData<List<RegistrationConsent>>()
    val consentLiveData: LiveData<List<RegistrationConsent>> get() = _consentLiveData

    init {
        getConsentList()
    }

    private fun getConsentList()
    {
        viewModelScope.launch(ioDispatcher) {
            _consentLiveData.postValue(consentUseCase())
        }
    }
}