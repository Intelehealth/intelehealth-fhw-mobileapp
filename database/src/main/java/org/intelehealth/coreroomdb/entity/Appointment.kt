package org.intelehealth.coreroomdb.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "tbl_appointments")
data class Appointment(
    @PrimaryKey
    @SerializedName("uuid")
    var uuid: String? = null,

    @SerializedName("appointment_id")
    var appointmentId: Int = 0,

    @SerializedName("slot_day")
    var slotDay: String? = null,

    @SerializedName("slot_date")
    var slotDate: String? = null,

    @SerializedName("slot_js_date")
    var slotJsDate: String? = null,

    @SerializedName("slot_duration")
    var slotDuration: Int = 0,

    @SerializedName("slot_duration_unit")
    var slotDurationUnit: String? = null,

    @SerializedName("slot_time")
    var slotTime: String? = null,

    @SerializedName("speciality")
    var speciality: String? = null,


    @SerializedName("user_uuid")
    var userUuid: String? = null,

    @SerializedName("dr_name")
    var drName: String? = null,

    @SerializedName("visit_uuid")
    var visitUuid: String? = null,

    @SerializedName("patient_name")
    var patientName: String? = null,

    @SerializedName("open_mrs_id")
    var openMrsId: String? = null,

    @SerializedName("patient_id")
    var patientId: String? = null,

    @SerializedName("status")
    var status: String? = null,

    @SerializedName("created_at")
    var createdAt: String? = null,

    @SerializedName("updated_at")
    var updatedAt: String? = null,

    @SerializedName("location_uuid")
    var locationUuid: String? = null,

    @SerializedName("hw_uuid")
    var hwUuid: String? = null,

    @SerializedName("reason")
    var reason: String? = null,

    @SerializedName("prev_slot_day")
    var prevDaySlot: String? = null,

    @SerializedName("prev_slot_date")
    var prevDateSlot: String? = null,

    @SerializedName("prev_slot_time")
    var prevTimeSlot: String? = null,

    @SerializedName("voided")
    var voided: Int = 0,

    @SerializedName("sync")
    var sync: Boolean = false,
) : Parcelable