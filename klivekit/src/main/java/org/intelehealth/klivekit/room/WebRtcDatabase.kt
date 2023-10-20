package org.intelehealth.klivekit.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import org.intelehealth.klivekit.room.dao.ChatDao
import com.codeglo.billingclient.room.dao.ChatDao
import com.codeglo.billingclient.room.dao.RtcCallLogDao
import org.intelehealth.klivekit.chat.model.ChatMessage

@Database(entities = [ChatMessage::class, RtcCallLog::class, ChatRoom::class], version = 1)
abstract class WebRtcDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao

    abstract fun chatRoomDao(): ChatRoomDao

    abstract fun rtcCallLogDao(): RtcCallLogDao

    companion object {

        @Volatile
        private var INSTANCE: WebRtcDatabase? = null

        const val DATABASE_NAME = "webrtc-db"

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
            val databaseName = "${getAppName(appContext)}.$DATABASE_NAME"
            return Room.databaseBuilder(appContext, WebRtcDatabase::class.java, databaseName)
                .fallbackToDestructiveMigration()
                .build()
        }

        private fun getAppName(context: Context) = getApplicationName(context).let {
            return@let it.replace(" ", "-").lowercase(Locale.getDefault())
        }
    }
}