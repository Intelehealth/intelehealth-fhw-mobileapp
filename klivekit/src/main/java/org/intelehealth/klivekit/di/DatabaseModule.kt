package com.codeglo.coyamore.di

import android.content.Context
import com.codeglo.coyamore.api.WebRtcDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@InstallIn(SingletonComponent::class)
@Module
class DatabaseModule {

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext context: Context) = WebRtcDatabase.getInstance(context)

    @Singleton
    @Provides
    fun provideChatDao(appDatabase: WebRtcDatabase) = appDatabase.chatDao()

}