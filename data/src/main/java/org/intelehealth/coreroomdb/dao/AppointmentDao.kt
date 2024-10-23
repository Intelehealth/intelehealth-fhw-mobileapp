package org.intelehealth.coreroomdb.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import org.intelehealth.coreroomdb.entity.Appointment

/**
 * Created by Vaghela Mithun R. on 02-04-2024 - 10:24.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
@Dao
interface AppointmentDao : CoreDao<Appointment> {
    @Query("SELECT * FROM tbl_appointments WHERE voided == 0")
    override fun getAll(): LiveData<List<Appointment>>

    @Query("SELECT * FROM tbl_appointments WHERE uuid = :uuid AND voided == 0")
    fun getByUuid(uuid: String): LiveData<Appointment>

    @Query("SELECT * FROM tbl_appointments WHERE appointment_id = :appointmentId AND voided == 0")
    fun getByAppointmentId(appointmentId: String): LiveData<Appointment>

    @Query("SELECT * FROM tbl_appointments WHERE patient_id = :patientId AND voided == 0")
    fun getByPatientId(patientId: String): LiveData<Appointment>

    @Query("SELECT * FROM tbl_appointments WHERE hw_uuid = :hwId AND voided == 0")
    fun getByHWId(hwId: String): LiveData<Appointment>

    @Query("SELECT * FROM tbl_appointments WHERE updated_at = :userId AND voided == 0")
    fun getByUserId(userId: String): LiveData<Appointment>

    @Query("SELECT * FROM tbl_appointments WHERE visit_uuid = :visitId AND voided == 0")
    fun getByVisitId(visitId: String): LiveData<Appointment>

    @Query("SELECT * FROM tbl_appointments WHERE open_mrs_id = :openMrsId AND voided == 0")
    fun getByOpenMrsId(openMrsId: String): LiveData<Appointment>
}