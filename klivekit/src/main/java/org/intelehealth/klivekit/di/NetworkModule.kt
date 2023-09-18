package org.intelehealth.klivekit.di

import org.intelehealth.klivekit.restapi.AuthInterceptor
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.intelehealth.klivekit.BuildConfig
import org.intelehealth.klivekit.di.qulifier.ApiClient
import org.intelehealth.klivekit.di.qulifier.RtcClient
import org.intelehealth.klivekit.restapi.WebRtcApiClient
import org.intelehealth.klivekit.utils.Constants
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class NetworkModule {

    @Singleton
    @Provides
    @ApiClient
    fun provideOkHttpApiClient(
        okHttpBuilder: OkHttpClient.Builder,
        authInterceptor: AuthInterceptor
    ): OkHttpClient = if (authInterceptor.hasToken()) {
        okHttpBuilder.addInterceptor(authInterceptor).build()
    } else {
        okHttpBuilder.build()
    }

    @Singleton
    @Provides
    @RtcClient
    fun provideOkHttpRtcClient(okHttpBuilder: OkHttpClient.Builder): OkHttpClient =
        okHttpBuilder.build()

    @Singleton
    @Provides
    fun provideOkHttpBuilder(interceptor: HttpLoggingInterceptor) =
        OkHttpClient.Builder().retryOnConnectionFailure(true)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(interceptor)

    @Singleton
    @Provides
    fun provideHttpLoggingInterceptor() = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
        else HttpLoggingInterceptor.Level.NONE
    }

    @Singleton
    @Provides
    fun provideGson(): Gson = Gson()

    @Singleton
    @Provides
    fun provideGsonConverterFactory(gson: Gson): GsonConverterFactory =
        GsonConverterFactory.create(gson)

    @Singleton
    @Provides
    fun provideRetrofitBuilder(
        okhttpClient: OkHttpClient,
        gsonConverterFactory: GsonConverterFactory
    ): Retrofit.Builder = Retrofit.Builder()
        .client(okhttpClient)
        .addConverterFactory(gsonConverterFactory)


    @Provides
    fun provideWebRtcApiClient(
        retrofitBuilder: Retrofit.Builder
    ): WebRtcApiClient = retrofitBuilder.baseUrl(Constants.BASE_URL)
        .build()
        .create(WebRtcApiClient::class.java)
}