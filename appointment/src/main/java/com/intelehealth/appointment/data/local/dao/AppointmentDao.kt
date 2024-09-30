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

    @Query("select * from tbl_appointments where visit_uuid=:key")
    suspend fun getRecordByVisitId(key: String): Appointments?

/*    @Query("SELECT count(*) from  tbl_appointments a where p.uuid = a.patient_id \"\n" +
            "                        + \"AND a.status = 'booked' \"\n" +
            "                        + \"AND  (datetime(a.slot_js_date) >= datetime('now'))")
    suspend fun getUpcomingAppointmentCount(): Int?*/
}