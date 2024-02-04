package com.intelehealth.coyamore.di

import android.content.Context
import org.intelehealth.klivekit.room.WebRtcDatabase
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

    @Singleton
    @Provides
    fun provideRtcCallLogDao(appDatabase: WebRtcDatabase) = appDatabase.rtcCallLogDao()

}