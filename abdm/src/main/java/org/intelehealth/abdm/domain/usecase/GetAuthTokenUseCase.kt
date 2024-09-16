package org.intelehealth.abdm.domain.usecase

import org.intelehealth.abdm.domain.repository.AuthTokenRepository
import javax.inject.Inject

class GetAuthTokenUseCase @Inject constructor(private val authTokenRepository: AuthTokenRepository){
    suspend operator fun invoke() = authTokenRepository.getAuthToken()
}