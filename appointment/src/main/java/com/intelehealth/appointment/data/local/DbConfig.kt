package com.intelehealth.appointment.data.local

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
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