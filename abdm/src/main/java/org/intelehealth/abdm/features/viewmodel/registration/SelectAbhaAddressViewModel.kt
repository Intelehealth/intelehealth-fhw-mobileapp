package org.intelehealth.abdm.features.viewmodel.registration

import dagger.hilt.android.lifecycle.HiltViewModel
import org.intelehealth.abdm.domain.usecase.GetAuthTokenUseCase
import org.intelehealth.abdm.domain.usecase.registration.AbhaSuggestionListUseCase
import org.intelehealth.abdm.domain.usecase.registration.EnrollAbhaAddressUseCase
import org.intelehealth.abdm.features.base.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class SelectAbhaAddressViewModel @Inject constructor(
    private val getAuthTokenUseCase: GetAuthTokenUseCase,
    private val abhaSuggestionListUseCase: AbhaSuggestionListUseCase,
    private val enrollAbhaAddressUseCase: EnrollAbhaAddressUseCase
) : BaseViewModel() {

}