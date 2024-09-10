package org.intelehealth.abdm.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import org.intelehealth.abdm.data.registration.RegistrationConsentRepositoryImp
import org.intelehealth.abdm.domain.registration.RegistrationConsentRepository

@Module
@InstallIn(ViewModelComponent::class)
abstract class DataModule {
    @Binds
    abstract fun bindRegistrationConsentRepository(
        repositoryImp: RegistrationConsentRepositoryImp
    ): RegistrationConsentRepository
}