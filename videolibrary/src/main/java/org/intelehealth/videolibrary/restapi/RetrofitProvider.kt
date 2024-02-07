package org.intelehealth.videolibrary.restapi

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.intelehealth.videolibrary.utils.VideoLibraryManager
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitProvider {

    private val loggingInterceptor: HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient: OkHttpClient = OkHttpClient.Builder().apply {
        addInterceptor(loggingInterceptor)
    }.build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(VideoLibraryManager.getBaseUrlWithPort())
        .addConverterFactory(GsonConverterFactory.create())
        .client(httpClient)
        .build()


    val apiService: VideoLibraryApiClient = retrofit.create(VideoLibraryApiClient::class.java)

}