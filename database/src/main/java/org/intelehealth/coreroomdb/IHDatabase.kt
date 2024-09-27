package org.intelehealth.coreroomdb

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import org.intelehealth.coreroomdb.entity.*
import org.intelehealth.coreroomdb.dao.*


/**
 * Created by - Prajwal W. on 27/09/24.
 * Email: prajwalwaingankar@gmail.com
 * Mobile: +917304154312
 **/

@Database(entities = [Appointment::class, Concept::class, DrSpeciality::class,
    Encounter::class, FollowupScheduleNotification::class, LocalNotification::class,
    MediaRecord::class, Observation::class, Patient::class, PatientAttribute::class,
    PatientAttributeTypeMaster::class, PatientLocation::class, Provider::class,
    ProviderAttribute::class, RtcConnectionLog::class, UserCredentials::class,
    Visit::class, VisitAttribute::class],
    version = 1)

abstract class IHDatabase: RoomDatabase() {
    abstract fun appointmentDao(): AppointmentDao
    abstract fun conceptDao(): ConceptDao
    abstract fun drSpecialityDao(): DrSpecialityDao
    abstract fun encounterDao(): EncounterDao
    abstract fun localNotificationDao(): LocalNotificationDao
    abstract fun mediaRecordDao(): MediaRecordDao
    abstract fun observationDao(): ObservationDao
    abstract fun patientDao(): PatientDao
    //  abstract fun coreDao(): CoreDao<T>

    companion object {
        @Volatile
        private var INSTANCE: IHDatabase? = null
     //   private val DATABASE_NAME = BuildConfig.FLAVOR_client + "-localrecords.db"
        private val DATABASE_NAME = "localrecords.db"

        /**
         * Get the singleton instance of the database.
         */
        fun getInstance(context: Context): IHDatabase =
            INSTANCE ?: synchronized(this) {    // synchronized - ensures that at a time only at the max 1 thread will be accessing the database operations.
                INSTANCE ?: buildDatabase(context.applicationContext).also {
                    INSTANCE = it
                }
            }

        /**
         * Set up the database configuration.
         * The SQLite database is only created when it's accessed for the first time.
         */
        private fun buildDatabase(appContext: Context): IHDatabase {
            val databaseName = "${appContext.packageName}.$DATABASE_NAME"
            return Room.databaseBuilder(appContext, IHDatabase::class.java, databaseName)
                .fallbackToDestructiveMigration()   // on migration if no migration scheme is provided than it will perform destructive migration.
                .build()
        }
    }



}