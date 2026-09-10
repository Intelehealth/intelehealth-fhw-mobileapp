package org.intelehealth.abdm.di.network

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.intelehealth.abdm.config.AbdmConfig
import org.intelehealth.abdm.data.remote.api.AbdmAuthApi
import org.intelehealth.abdm.data.remote.api.PatientApi
import org.intelehealth.abdm.di.qualifiers.AbdmLogging
import org.intelehealth.abdm.di.qualifiers.AuthClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthNetworkModule {
    @Provides
    @Singleton
    @AuthClient
    fun provideOkHttpClient(
        @AbdmLogging loggingInterceptor: HttpLoggingInterceptor,
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    @AuthClient
    fun provideRetrofit(
        @AuthClient okHttpClient: OkHttpClient,
        config: AbdmConfig,
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(config.baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideAbdmAuthApi(
        @AuthClient retrofit: Retrofit,
    ): AbdmAuthApi = retrofit.create(AbdmAuthApi::class.java)

    @Provides
    @Singleton
    fun providePatientApi(
        @AuthClient retrofit: Retrofit,
    ): PatientApi = retrofit.create(PatientApi::class.java)
}