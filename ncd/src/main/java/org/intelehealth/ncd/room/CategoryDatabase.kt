package org.intelehealth.ncd.room

import android.content.Context
import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import org.intelehealth.ncd.model.Patient
import org.intelehealth.ncd.model.PatientAttributes
import org.intelehealth.ncd.room.dao.PatientAttributeDao
import org.intelehealth.ncd.room.dao.PatientDao
import org.intelehealth.ncd.utils.getApplicationName
import java.io.File
import java.util.Locale

@Database(entities = [Patient::class, PatientAttributes::class], version = 4)
abstract class CategoryDatabase : RoomDatabase() {

    abstract fun patientDao(): PatientDao
    abstract fun patientAttributeDao(): PatientAttributeDao

    companion object {

        @Volatile
        private var INSTANCE: CategoryDatabase? = null

        @VisibleForTesting
        private val DATABASE_NAME = "ekal-localrecords.db"
        ///data/data/org.intelehealth.ekalarogya/databases/ekal-localrecords.db
        @JvmStatic
        fun getInstance(context: Context): CategoryDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context.applicationContext).also {
                    INSTANCE = it
                }
            }

        /**
         * Set up the database configuration.
         * The SQLite database is only created when it's accessed for the first time.
         */
        private fun buildDatabase(appContext: Context): CategoryDatabase {
            val dbFile = File("/data/data/org.intelehealth.ekalarogya/databases/ekal-localrecords.db")

          return  Room.databaseBuilder(appContext, CategoryDatabase::class.java, "ekal-localrecords.db")
                .createFromFile(dbFile) // ✅ Must use this!
                .build()

            /*  val dbFile = File("/data/data/org.intelehealth.ekalarogya/databases/ekal-localrecords.db")
              return Room.databaseBuilder(appContext, CategoryDatabase::class.java, "ekal-localrecords.db")
                  .createFromFile(dbFile)
                  .addCallback(object : RoomDatabase.Callback() {
                      override fun onOpen(db: SupportSQLiteDatabase) {
                          val cursor = db.query("SELECT sql FROM sqlite_master WHERE name = 'tbl_patient'")
                          if (cursor.moveToFirst()) {
                              Log.d("SCHEMA_CHECK", "tbl_patient: ${cursor.getString(0)}")
                          }
                          cursor.close()
                      }
                  })
                  .build()*/
          /*  val dbFile = appContext.getDatabasePath("/data/data/org.intelehealth.ekalarogya/databases/ekal-localrecords.db")
            return Room.databaseBuilder(appContext, CategoryDatabase::class.java, "/data/data/org.intelehealth.ekalarogya/databases/ekal-localrecords.db")
                .build()*///use this

            /*return Room.databaseBuilder(appContext, CategoryDatabase::class.java, dbFile.absolutePath)
                .createFromFile(dbFile)
                .build()*/
          /*  val databaseName = DATABASE_NAME
            return Room.databaseBuilder(appContext, CategoryDatabase::class.java, databaseName)
                //.fallbackToDestructiveMigration()
                .build()*/
        }

        private fun getAppName(context: Context) = getApplicationName(context).let {
            return@let it.replace(" ", "-").lowercase(Locale.getDefault())
        }
    }

}