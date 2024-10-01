package com.intelehealth.appointment.data.provider

import com.google.gson.Gson
import com.intelehealth.appointment.AppointmentBuilder
import com.intelehealth.appointment.BuildConfig
import com.intelehealth.appointment.data.remote.AppointmentWebClient
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.intelehealth.app.networkApiCalls.interceptors.TokenSetupInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Created by Tanvir Hasan 20-09-24.
 * Email :mhasan@intelehealth.org
 **/
object WebClientProvider {
    private var apiClient: AppointmentWebClient? = null

    fun getApiClient(): AppointmentWebClient = apiClient ?: synchronized(this) {
        apiClient ?: buildClient().also {
            apiClient = it
        }
    }

    private fun buildClient(): AppointmentWebClient {
        return provideWebRtcApiClient(
            provideRetrofitBuilder(
                provideOkHttpRtcClient(provideOkHttpBuilder(provideHttpLoggingInterceptor(),TokenSetupInterceptor())),
                provideGsonConverterFactory(provideGson())
            )
        )
    }

    private fun provideOkHttpRtcClient(okHttpBuilder: OkHttpClient.Builder): OkHttpClient =
        okHttpBuilder.build()

    private fun provideOkHttpBuilder(interceptor: HttpLoggingInterceptor,tokenSetupInterceptor: TokenSetupInterceptor) =
        OkHttpClient.Builder().retryOnConnectionFailure(true)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(interceptor)
            .addInterceptor(tokenSetupInterceptor)

    private fun provideHttpLoggingInterceptor() = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
        else HttpLoggingInterceptor.Level.NONE
    }

    private fun provideGson(): Gson = Gson()


    private fun provideGsonConverterFactory(gson: Gson): GsonConverterFactory =
        GsonConverterFactory.create(gson)

    private fun provideRetrofitBuilder(
        okhttpClient: OkHttpClient,
        gsonConverterFactory: GsonConverterFactory
    ): Retrofit.Builder = Retrofit.Builder()
        .client(okhttpClient)
        .addConverterFactory(gsonConverterFactory)


    private fun provideWebRtcApiClient(
        retrofitBuilder: Retrofit.Builder
    ): AppointmentWebClient = retrofitBuilder
        .baseUrl(AppointmentBuilder.baseUrl)
        .build()
        .create(AppointmentWebClient::class.java)
}