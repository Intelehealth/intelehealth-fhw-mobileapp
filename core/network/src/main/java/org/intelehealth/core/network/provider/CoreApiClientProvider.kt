package org.intelehealth.core.network.provider

import com.github.ajalt.timberkt.BuildConfig
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.intelehealth.core.network.CoreApiClient
import org.intelehealth.core.network.helper.CoreApiConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


/**
 * Created by - Prajwal W. on 10/10/24.
 * Email: prajwalwaingankar@gmail.com
 * Mobile: +917304154312
 **/

object CoreApiClientProvider {
    private var coreApiClient: CoreApiClient? = null

    fun getCoreApiClient(): CoreApiClient = coreApiClient ?: synchronized(this) {
        coreApiClient ?: buildCoreApiClient().also {
            coreApiClient = it
        }
    }

    fun changeApiBaseUrl(newApiBaseUrl: String) {   // TODO: how to handle this changing baseurl throughout app flow?
        CoreApiConfig.baseUrl = newApiBaseUrl
        coreApiClient = null    // TODO: reset the instance.
        buildCoreApiClient().also {
            coreApiClient = it
        }

        /*ApiClient.builder = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(ApiClient.gson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .baseUrl(ApiClient.apiBaseUrl)*/
    }

    private fun provideCoreApiClient(retrofitBuilder: Retrofit.Builder): CoreApiClient =
            retrofitBuilder
                .baseUrl(CoreApiConfig.baseUrl)    // TODO: create a core config file for baseURL as using config module class was creating CD.
                .build()
                .create(CoreApiClient::class.java)

    private fun provideRetrofitBuilder(provideOkHttpClient: OkHttpClient,
        provideGsonConverterFactory: GsonConverterFactory): Retrofit.Builder = Retrofit.Builder()
        .client(provideOkHttpClient)
        .addConverterFactory(provideGsonConverterFactory)


    private fun provideOkHttpClient(provideOkHttpBuilder: OkHttpClient.Builder)
    : OkHttpClient = provideOkHttpBuilder.build()

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

    private fun provideGsonConverterFactory(gson: Gson)
    : GsonConverterFactory = GsonConverterFactory.create(gson)

    private fun buildCoreApiClient(): CoreApiClient {
        return provideCoreApiClient(
            provideRetrofitBuilder(
                provideOkHttpClient(provideOkHttpBuilder(provideHttpLoggingInterceptor())),
                provideGsonConverterFactory(provideGson())
            )
        )
    }


}