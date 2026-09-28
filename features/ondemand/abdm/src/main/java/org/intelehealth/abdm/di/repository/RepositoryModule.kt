package org.intelehealth.abdm.di.repository

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.intelehealth.abdm.data.repository.AbdmAuthRepositoryImpl
import org.intelehealth.abdm.data.repository.AbhaCreateRepositoryImpl
import org.intelehealth.abdm.data.repository.AbhaProfileRepositoryImpl
import org.intelehealth.abdm.data.repository.AbhaSuggestionsRepositoryImpl
import org.intelehealth.abdm.data.repository.AbhaVerifyRepositoryImpl
import org.intelehealth.abdm.data.repository.PatientRepositoryImpl
import org.intelehealth.abdm.domain.repository.AbdmAuthRepository
import org.intelehealth.abdm.domain.repository.AbhaCreateRepository
import org.intelehealth.abdm.domain.repository.AbhaProfileRepository
import org.intelehealth.abdm.domain.repository.AbhaSuggestionsRepository
import org.intelehealth.abdm.domain.repository.AbhaVerifyRepository
import org.intelehealth.abdm.domain.repository.PatientRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindAbdmAuthRepository(impl: AbdmAuthRepositoryImpl): AbdmAuthRepository

    @Binds
    @Singleton
    abstract fun bindAbhaSuggestionsRepository(impl: AbhaSuggestionsRepositoryImpl): AbhaSuggestionsRepository

    @Binds
    @Singleton
    abstract fun bindAbhaProfileRepository(impl: AbhaProfileRepositoryImpl): AbhaProfileRepository

    @Binds
    @Singleton
    abstract fun bindAbhaCreateRepository(impl: AbhaCreateRepositoryImpl): AbhaCreateRepository

    @Binds
    @Singleton
    abstract fun bindAbhaVerifyRepository(impl: AbhaVerifyRepositoryImpl): AbhaVerifyRepository

    @Binds
    @Singleton
    abstract fun bindPatientRepository(impl: PatientRepositoryImpl): PatientRepository
}