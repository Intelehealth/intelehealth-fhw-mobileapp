package org.intelehealth.abdm.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.intelehealth.abdm.AbhaFeatureImpl
import org.intelehealth.abdm.api.AbhaFeature

/**
 * Plugs this module into the host: its presence on the classpath is what turns the host's optional
 * AbhaFeature binding into a real one.
 */
@Module
@InstallIn(SingletonComponent::class)
internal abstract class AbhaFeatureModule {
    @Binds
    abstract fun bindAbhaFeature(impl: AbhaFeatureImpl): AbhaFeature
}
