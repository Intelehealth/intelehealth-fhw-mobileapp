package org.intelehealth.abdm.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import org.intelehealth.abdm.data.repository.AuthTokenRepositoryImp
import org.intelehealth.abdm.data.repository.registration.EnrollAbhaAddressRepositoryImp
import org.intelehealth.abdm.data.repository.registration.RegisterAbdmRepositoryImp
import org.intelehealth.abdm.data.repository.registration.RegistrationConsentRepositoryImp
import org.intelehealth.abdm.domain.repository.AuthTokenRepository
import org.intelehealth.abdm.domain.repository.registration.EnrollAbhaAddressRepository
import org.intelehealth.abdm.domain.repository.registration.RegisterAbdmRepository
import org.intelehealth.abdm.domain.repository.registration.RegistrationConsentRepository


@Module
@InstallIn(ViewModelComponent::class)
abstract class DataModule {
    @Binds
    abstract fun bindRegistrationConsentRepository(
        repositoryImp: RegistrationConsentRepositoryImp
    ): RegistrationConsentRepository
    @Binds
    abstract fun bindAuthTokenRepository(
        repositoryImp: AuthTokenRepositoryImp
    ): AuthTokenRepository

    @Binds
    abstract fun bindAbdmRepository(
        repositoryImp: RegisterAbdmRepositoryImp
    ): RegisterAbdmRepository


    @Binds
    abstract fun bindEnrollAbhaAddress(
        repositoryImp: EnrollAbhaAddressRepositoryImp
    ): EnrollAbhaAddressRepository
}