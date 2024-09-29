package com.intelehealth.appointment.di

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import androidx.room.Room
import com.intelehealth.appointment.BuildConfig
import com.intelehealth.appointment.data.local.DbConfig
import com.intelehealth.appointment.data.local.SharedPref
import com.intelehealth.appointment.data.repository.AppointmentSyncRepo
import dagger.Module
import dagger.Provides
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Created By Tanvir Hasan
 * Email: tanvirhasan553@gmail.com
 */
@Module
@InstallIn(SingletonComponent::class)
class AppModule {
    /**
     * providing shared SharedPreferences instance here
     */
    @Provides
    @Singleton
    internal fun getSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }

    /**
     * get shared pref instance here
     */
    @Provides
    @Singleton
    internal fun getSharedPref(sharedPreferences: SharedPreferences): SharedPref{
        return SharedPref(sharedPreferences)
    }

    /**
     * getting room db instance here
     */
    @Provides
    @Singleton
    internal fun getRoomDb(@ApplicationContext context: Context): DbConfig{
        return Room.databaseBuilder(
            context, DbConfig::class.java,
            "appointment-db"
        ).build();
    }
}