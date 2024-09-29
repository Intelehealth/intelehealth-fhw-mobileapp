package com.intelehealth.appointment.data.local.dao

import androidx.room.Dao
import com.intelehealth.appointment.data.local.entity.Appointments

@Dao
interface AppointmentDao : CoreDao<Appointments>{

    override suspend fun getAllRecord(): List<Appointments> {
        TODO("Not yet implemented")
    }

    override suspend fun getRecord(key: String): Appointments {
        TODO("Not yet implemented")
    }
}