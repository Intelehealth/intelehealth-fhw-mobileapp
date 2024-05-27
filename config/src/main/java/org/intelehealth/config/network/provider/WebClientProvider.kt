package org.intelehealth.config.network.provider

import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.intelehealth.config.BuildConfig
import org.intelehealth.config.Config
import org.intelehealth.config.network.WebClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Created by Vaghela Mithun R. on 16-09-2023 - 20:04.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
object WebClientProvider {
    private var apiClient: WebClient? = null

    fun getApiClient(): WebClient = apiClient ?: synchronized(this) {
        apiClient ?: buildWebRtcClient().also {
            apiClient = it
        }
    }

    private fun buildWebRtcClient(): WebClient {
        return provideWebRtcApiClient(
            provideRetrofitBuilder(
                provideOkHttpRtcClient(provideOkHttpBuilder(provideHttpLoggingInterceptor())),
                provideGsonConverterFactory(provideGson())
            )
        )
    }

    private fun provideOkHttpRtcClient(okHttpBuilder: OkHttpClient.Builder): OkHttpClient =
        okHttpBuilder.build()

    private fun provideOkHttpBuilder(interceptor: HttpLoggingInterceptor) =
        OkHttpClient.Builder().retryOnConnectionFailure(true)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(interceptor)

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
    ): WebClient = retrofitBuilder
        .baseUrl(Config.baseUrl)
        .build()
        .create(WebClient::class.java)
}