package com.intelehealth.appointment.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

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
    val sync: String?
)
