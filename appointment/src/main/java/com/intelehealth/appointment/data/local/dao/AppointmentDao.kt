package com.intelehealth.appointment.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.SkipQueryVerification
import androidx.sqlite.db.SupportSQLiteQuery
import com.intelehealth.appointment.data.local.entity.Appointments

@Dao
interface AppointmentDao : CoreDao<Appointments> {

    /*    @Query("ATTACH DATABASE :dbPath AS secondDb")
        suspend fun attachDb(dbPath:String)*/

    @Query("select * from tbl_appointments")
    suspend fun getAllRecord(): List<Appointments>

    @Query("select * from tbl_appointments where appointment_id=:key")
    suspend fun getRecord(key: String): Appointments

    @Query("select * from tbl_appointments where visit_uuid=:key")
    suspend fun getRecordByVisitId(key: String): Appointments?

    @SkipQueryVerification
    @Query("SELECT count(*) from appModuleDb.tbl_patient p, main.tbl_appointments a where p.uuid = a.patient_id  AND  (datetime(a.slot_js_date) < datetime('now'))")
    suspend fun getUpcomingAppointmentCount(): Int?
}