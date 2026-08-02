package org.intelehealth.abdm.di.network

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.intelehealth.abdm.config.AbdmConfig
import org.intelehealth.abdm.data.remote.api.AbhaCreateApi
import org.intelehealth.abdm.data.remote.api.AbhaProfileApi
import org.intelehealth.abdm.data.remote.api.AbhaSuggestionsApi
import org.intelehealth.abdm.data.remote.api.AbhaVerifyApi
import org.intelehealth.abdm.data.remote.interceptor.AuthInterceptor
import org.intelehealth.abdm.di.qualifiers.AbdmLogging
import org.intelehealth.abdm.di.qualifiers.MainClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MainNetworkModule {
    @Provides
    @Singleton
    @MainClient
    fun provideOkHttpClient(
        @AbdmLogging loggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: AuthInterceptor,
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    @MainClient
    fun provideRetrofit(
        @MainClient okHttpClient: OkHttpClient,
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
    fun provideAbhaCreateApi(
        @MainClient retrofit: Retrofit,
    ): AbhaCreateApi = retrofit.create(AbhaCreateApi::class.java)

    @Provides
    @Singleton
    fun provideAbhaProfileApi(
        @MainClient retrofit: Retrofit,
    ): AbhaProfileApi = retrofit.create(AbhaProfileApi::class.java)

    @Provides
    @Singleton
    fun provideAbhaSuggestionsApi(
        @MainClient retrofit: Retrofit,
    ): AbhaSuggestionsApi = retrofit.create(AbhaSuggestionsApi::class.java)

    @Provides
    @Singleton
    fun provideAbhaVerifyApi(
        @MainClient retrofit: Retrofit,
    ): AbhaVerifyApi = retrofit.create(AbhaVerifyApi::class.java)
}