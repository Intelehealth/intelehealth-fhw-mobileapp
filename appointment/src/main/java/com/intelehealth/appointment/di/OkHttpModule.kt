package com.intelehealth.appointment.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Created By Tanvir Hasan
 * Email: tanvirhasan553@gmail.com
 */
@Module
@InstallIn(SingletonComponent::class)
object OkHttpModule {
    /*@Provides
    internal fun okHttpClient(
        cache: Cache,
        interceptor: HttpLoggingInterceptor,
    ): OkHttpClient {
        return OkHttpClient().newBuilder()
            .addInterceptor(interceptor)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .cache(cache)
            .build()
    }

    @Provides
    internal fun cache(file: File) = Cache(file, (10 * 1000 * 1000).toLong()) //10 MB


    @Provides
    internal fun file(@ApplicationContext context: Context): File {
        val file = File(context.cacheDir, "HttpCache")
        file.mkdirs()
        return file
    }

    @Provides
    internal fun loggingInterceptor(): HttpLoggingInterceptor {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        return loggingInterceptor
    }*/

/*    @Provides
    internal fun internetConnectionInterceptor() = NetworkConnectionInterceptor()*/

/*    @Provides
    internal fun setupTokenInterceptor(sharedPref: SharedPref) = TokenSetupInterceptor(sharedPref)*/

/*    @Provides
    internal fun logoutInterceptor() = LogoutInterceptor()*/

}