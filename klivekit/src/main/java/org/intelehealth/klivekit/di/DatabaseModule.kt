package org.intelehealth.klivekit.di

import android.content.Context
import androidx.room.Room
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
    fun provideDatabase(@ApplicationContext context: Context): WebRtcDatabase {
        val databaseName = "${context.packageName}.${WebRtcDatabase.DATABASE_NAME}"
        return Room.databaseBuilder(context, WebRtcDatabase::class.java, databaseName)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Singleton
    @Provides
    fun provideChatDao(appDatabase: WebRtcDatabase) = appDatabase.chatDao()

    @Singleton
    @Provides
    fun provideChatRoom(appDatabase: WebRtcDatabase) = appDatabase.chatRoomDao()
}