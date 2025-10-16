package org.intelehealth.ncd.room

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import org.intelehealth.ncd.model.Encounter
import org.intelehealth.ncd.model.Obs
import org.intelehealth.ncd.model.Patient
import org.intelehealth.ncd.model.PatientAttributes
import org.intelehealth.ncd.model.Visit
import org.intelehealth.ncd.model.VisitAttributes
import org.intelehealth.ncd.room.dao.GeneralTabDao
import org.intelehealth.ncd.room.dao.PatientAttributeDao
import org.intelehealth.ncd.room.dao.PatientDao
import org.intelehealth.ncd.room.dao.VisitDao
import org.intelehealth.ncd.utils.getApplicationName
import java.io.File
import java.util.Locale

@Database(entities = [Patient::class, PatientAttributes::class, Visit::class, Encounter::class, Obs::class, VisitAttributes::class], version = 4)
abstract class CategoryDatabase : RoomDatabase() {

    abstract fun patientDao(): PatientDao
    abstract fun patientAttributeDao(): PatientAttributeDao
    abstract fun visitDao(): VisitDao
    abstract fun generalTabDao(): GeneralTabDao

    companion object {

        @Volatile
        private var INSTANCE: CategoryDatabase? = null

        @VisibleForTesting
        private val DATABASE_NAME = "ekal-localrecords.db"

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
                .createFromFile(dbFile)
                .build()
            /*val dbFile = appContext.getDatabasePath("ekal-localrecords.db")

            return Room.databaseBuilder(appContext, CategoryDatabase::class.java, "ekal-localrecords.db")
                .createFromFile(dbFile)
                .build()*/
           /* val databaseName = DATABASE_NAME
            return Room.databaseBuilder(appContext, CategoryDatabase::class.java, databaseName)
                .fallbackToDestructiveMigration()
                .build()*/
        }

        private fun getAppName(context: Context) = getApplicationName(context).let {
            return@let it.replace(" ", "-").lowercase(Locale.getDefault())
        }
    }

}