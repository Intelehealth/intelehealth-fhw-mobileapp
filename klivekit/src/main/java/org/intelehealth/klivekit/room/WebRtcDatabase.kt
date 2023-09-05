package com.codeglo.coyamore.api

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import org.intelehealth.klivekit.room.dao.ChatDao
import org.intelehealth.klivekit.chat.model.ChatMessage
import org.intelehealth.klivekit.chat.model.ChatRoom

@Database(entities = [ChatMessage::class], version = 1)
abstract class WebRtcDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao

    abstract fun chatRoom(): ChatRoom

    companion object {

        @Volatile
        private var INSTANCE: WebRtcDatabase? = null

        const val DATABASE_NAME = "webrtc-db"

        fun getInstance(context: Context): WebRtcDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context.applicationContext).also {
                    INSTANCE = it
                }
            }

        /**
         * Set up the database configuration.
         * The SQLite database is only created when it's accessed for the first time.
         */
        private fun buildDatabase(appContext: Context): WebRtcDatabase {
            val databaseName = "${appContext.packageName}.$DATABASE_NAME"
            return Room.databaseBuilder(appContext, WebRtcDatabase::class.java, databaseName)
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}