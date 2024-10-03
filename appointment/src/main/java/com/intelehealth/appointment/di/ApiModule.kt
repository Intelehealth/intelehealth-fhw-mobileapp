package com.intelehealth.appointment.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.intelehealth.appointment.data.remote.AppointmentWebClient
import retrofit2.Retrofit
import javax.inject.Named


/**
 * Created By Tanvir Hasan
 * Email: tanvirhasan553@gmail.com
 */
@Module
@InstallIn(SingletonComponent::class)
object ApiModule {
    @Provides
    internal fun apiService(@Named("AppointmentRetrofit") retrofit: Retrofit): AppointmentWebClient {
        return retrofit.create(AppointmentWebClient::class.java)
    }
}