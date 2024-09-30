package com.intelehealth.appointment.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.intelehealth.appointment.data.local.entity.Appointments

@Dao
interface AppointmentDao : CoreDao<Appointments>{

    @Query("select * from tbl_appointments")
    suspend fun getAllRecord(): List<Appointments>

    @Query("select * from tbl_appointments where appointment_id=:key")
    suspend fun getRecord(key: String): Appointments
}