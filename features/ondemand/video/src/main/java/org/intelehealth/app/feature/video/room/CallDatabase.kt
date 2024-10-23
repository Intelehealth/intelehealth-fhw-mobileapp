package org.intelehealth.app.feature.video.room

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import org.intelehealth.core.utils.extensions.appName
import org.intelehealth.app.feature.video.room.dao.VideoCallLogDao
import org.intelehealth.app.feature.video.model.VideoCallLog

@Database(entities = [VideoCallLog::class], version = 1, exportSchema = false)
abstract class CallDatabase : RoomDatabase() {

    abstract fun rtcCallLogDao(): VideoCallLogDao

    companion object {

        @Volatile
        private var INSTANCE: CallDatabase? = null

        @VisibleForTesting
        private val DATABASE_NAME = "webrtc-db"

        @JvmStatic
        fun getInstance(context: Context): CallDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context.applicationContext).also {
                    INSTANCE = it
                }
            }

        /**
         * Set up the database configuration.
         * The SQLite database is only created when it's accessed for the first time.
         */
        private fun buildDatabase(appContext: Context): CallDatabase {
            val databaseName = "${appContext.appName()}.$DATABASE_NAME"
            return Room.databaseBuilder(appContext, CallDatabase::class.java, databaseName)
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}