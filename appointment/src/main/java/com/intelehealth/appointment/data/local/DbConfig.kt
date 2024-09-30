package com.intelehealth.appointment.data.local

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.room.Database
import androidx.room.Room.databaseBuilder
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.intelehealth.appointment.data.local.dao.AppointmentDao
import com.intelehealth.appointment.data.local.entity.Appointments
import java.util.Locale


/**
 * Created by Tanvir Hasan. on 25-09-2024 - 15:46.
 * Email : mhasan@intelehealth.org
 **/
@Database(
    entities = [
        Appointments::class
    ],
    version = 1,
    exportSchema = false
)
abstract class DbConfig : RoomDatabase() {

    abstract fun appointmentsDao(): AppointmentDao

    /*abstract fun specializationDao(): SpecializationDao

    abstract fun languageDao(): LanguageDao

    abstract fun patientRegFieldDao(): PatientRegFieldDao

    abstract fun patientVitalDao(): PatientVitalDao

    abstract fun featureActiveStatusDao(): FeatureActiveStatusDao*/



    companion object {

        @Volatile
        private var INSTANCE: DbConfig? = null

        @VisibleForTesting
        private val DATABASE_NAME = "appointment-db"

        @JvmStatic
        fun getInstance(context: Context): DbConfig =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context.applicationContext).also {
                    INSTANCE = it
                }
            }

        /**
         * Set up the database configuration.
         * The SQLite database is only created when it's accessed for the first time.
         */
        private fun buildDatabase(appContext: Context): DbConfig {
            val databaseName = "${appContext.packageName}.$DATABASE_NAME"
            return  INSTANCE ?: databaseBuilder(appContext, DbConfig::class.java, databaseName)
                .addCallback(object : Callback() {
                override fun onOpen(db: SupportSQLiteDatabase) {
                    attach(
                        "ida-localrecords",
                        "/data/data/org.intelehealth.app/databases"
                    )
                    super.onOpen(db)
                }
            })
                .build()
        }

        private fun attach(databaseName: String, databasePath: String) {

            val sql = "ATTACH DATABASE '$databasePath/$databaseName.db' AS \"ida_localrecords\";"
            INSTANCE!!.mDatabase!!.execSQL(sql)
        }

        private fun getAppName(context: Context) = getApplicationName(context).let {
            return@let it.replace(" ", "-").lowercase(Locale.getDefault())
        }

        private fun getApplicationName(context: Context): String {
            val applicationInfo = context.applicationInfo
            val stringId = applicationInfo.labelRes
            return if (stringId == 0) applicationInfo.nonLocalizedLabel.toString()
            else context.getString(stringId)
        }
    }
}