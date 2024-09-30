package com.intelehealth.appointment.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.intelehealth.appointment.data.remote.response.AppointmentInfo

@Entity(tableName = "tbl_appointments")
data class Appointments(
    @PrimaryKey
    val uuid: String,
    val appointment_id: Int?,
    val slot_day: String?,
    val slot_date: String?,
    val slot_js_date: String?,
    val slot_duration: Int?,
    val slot_duration_unit: String?,
    val slot_time: String?,
    val speciality: String?,
    val user_uuid: String?,
    val dr_name: String?,
    val visit_uuid: String?,
    val patient_id: String?,
    val patient_name: String?,
    val open_mrs_id: String?,
    val status: String?,
    val location_uuid: String?,
    val hw_uuid: String?,
    val reason: String?,
    val created_at: String?,
    val updated_at: String?,
    val prev_slot_day: String?,
    val prev_slot_date: String?,
    val prev_slot_time: String?,
    val voided: String?,
    val sync: Boolean?
) {
    companion object {
        fun toAppointments(it: MutableList<AppointmentInfo>): List<Appointments> {
            val list = mutableListOf<Appointments>()
            it.forEach {
                var rescheduledAppointmentsModel: com.intelehealth.appointment.data.remote.response.RescheduledAppointmentsModel? = null
                if (it.rescheduledAppointments != null && it.rescheduledAppointments.size > 0) {
                    val rescheduledSize: Int = it.rescheduledAppointments.size
                    rescheduledAppointmentsModel =
                        it.rescheduledAppointments[rescheduledSize - 1]
                }

                list.add(Appointments(
                    uuid = it.uuid,
                    appointment_id = it.id,
                    slot_day = it.slotDay,
                    slot_date = it.slotDate,
                    slot_js_date = it.slotJsDate,
                    slot_duration = it.slotDuration,
                    slot_duration_unit = it.slotDurationUnit,
                    slot_time = it.slotTime,
                    speciality = it.speciality,
                    user_uuid = it.userUuid,
                    dr_name = it.drName,
                    visit_uuid = it.visitUuid,
                    patient_id = it.patientId,
                    patient_name = it.patientName,
                    open_mrs_id = it.openMrsId,
                    status = it.status,
                    location_uuid = "",
                    hw_uuid = "",
                    reason = "",
                    created_at = it.createdAt,
                    updated_at = it.updatedAt,
                    prev_slot_day = rescheduledAppointmentsModel?.slotDay,
                    prev_slot_date = rescheduledAppointmentsModel?.slotDate,
                    prev_slot_time = rescheduledAppointmentsModel?.slotTime,
                    voided = "",
                    sync = true

                ))
            }
            return list
        }

        fun toAppointment(it: AppointmentInfo): Appointments {

                return  Appointments(
                    uuid = it.uuid,
                    appointment_id = it.id,
                    slot_day = it.slotDay,
                    slot_date = it.slotDate,
                    slot_js_date = it.slotJsDate,
                    slot_duration = it.slotDuration,
                    slot_duration_unit = it.slotDurationUnit,
                    slot_time = it.slotTime,
                    speciality = it.speciality,
                    user_uuid = it.userUuid,
                    dr_name = it.drName,
                    visit_uuid = it.visitUuid,
                    patient_id = it.patientId,
                    patient_name = it.patientName,
                    open_mrs_id = it.openMrsId,
                    status = it.status,
                    location_uuid = "",
                    hw_uuid = "",
                    reason = "",
                    created_at = it.createdAt,
                    updated_at = it.updatedAt,
                    prev_slot_day = it.slotDay,
                    prev_slot_date = it.slotDate,
                    prev_slot_time = it.slotTime,
                    voided = "",
                    sync = true

                )
        }
    }
}
