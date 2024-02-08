package org.intelehealth.videolibrary.room

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import org.intelehealth.videolibrary.utils.getApplicationName
import java.util.Locale


@Database(entities = [], version = 1)
abstract class VideoLibraryDatabase : RoomDatabase() {

    companion object {

        @Volatile
        private var INSTANCE: VideoLibraryDatabase? = null

        @VisibleForTesting
        private val DATABASE_NAME = "video-library-db"

        @JvmStatic
        fun getInstance(context: Context): VideoLibraryDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        
        /**
         * Set up the database configuration.
         * The SQLite database is only created when it's accessed for the first time.
         */
        private fun buildDatabase(appContext: Context): VideoLibraryDatabase {
            val databaseName = "${getAppName(appContext)}.$DATABASE_NAME"
            return Room.databaseBuilder(appContext, VideoLibraryDatabase::class.java, databaseName)
                .fallbackToDestructiveMigration()
                .build()
        }

        private fun getAppName(context: Context) = getApplicationName(context).let {
            return@let it.replace(" ", "-").lowercase(Locale.getDefault())
        }
    }
}