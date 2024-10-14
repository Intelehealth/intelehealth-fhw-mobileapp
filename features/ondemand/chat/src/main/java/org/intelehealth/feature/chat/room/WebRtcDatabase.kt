package org.intelehealth.feature.chat.room

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import org.intelehealth.core.utils.extensions.appName
import org.intelehealth.feature.chat.model.ChatMessage
import org.intelehealth.feature.chat.room.dao.ChatDao

@Database(entities = [ChatMessage::class], version = 1, exportSchema = false)
abstract class WebRtcDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao

    companion object {

        @Volatile
        private var INSTANCE: WebRtcDatabase? = null

        @VisibleForTesting
        private val DATABASE_NAME = "webrtc-db"

        @JvmStatic
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
            val databaseName = "${appContext.appName()}.$DATABASE_NAME"
            return Room.databaseBuilder(appContext, WebRtcDatabase::class.java, databaseName)
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}