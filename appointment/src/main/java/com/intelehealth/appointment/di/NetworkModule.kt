package com.intelehealth.appointment.di

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.intelehealth.appointment.AppointmentBuilder
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Created By Tanvir Hasan
 * Email: tanvirhasan553@gmail.com
 */
@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {
    /**
     * retrofit instance
     */
    @Provides
    internal fun retrofit(@ApplicationContext context: Context, okHttpClient: OkHttpClient, coroutineCallAdapterFactory: CoroutineCallAdapterFactory, gsonConverterFactory: GsonConverterFactory, gson: Gson): Retrofit {
        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(AppointmentBuilder.baseUrl)
            .addConverterFactory(gsonConverterFactory)
            .addCallAdapterFactory(coroutineCallAdapterFactory)
            .build()
    }

    @Provides
    internal fun gsonConverterFactory(gson: Gson): GsonConverterFactory {
        return GsonConverterFactory.create(gson)
    }

    @Provides
    internal fun  getCoroutineCallAdapterFactory (): CoroutineCallAdapterFactory {
        return CoroutineCallAdapterFactory()
    }

    @Provides
    internal fun gson(): Gson {
        return GsonBuilder().setLenient().serializeNulls().setPrettyPrinting().create()
    }
}