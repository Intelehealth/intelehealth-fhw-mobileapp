package org.intelehealth.coreroomdb.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "tbl_appointments")
data class Appointment(
    @PrimaryKey
    @SerializedName("uuid") var uuid: String,
    @ColumnInfo("appointment_id") @SerializedName("appointment_id") var appointmentId: Int = 0,
    @ColumnInfo("slot_day") @SerializedName("slot_day") var slotDay: String? = null,
    @ColumnInfo("slot_date") @SerializedName("slot_date") var slotDate: String? = null,
    @ColumnInfo("slot_js_date") @SerializedName("slot_js_date") var slotJsDate: String? = null,
    @ColumnInfo("slot_duration") @SerializedName("slot_duration") var slotDuration: Int = 0,
    @ColumnInfo("slot_duration_unit") @SerializedName("slot_duration_unit") var slotDurationUnit: String? = null,
    @ColumnInfo("slot_time") @SerializedName("slot_time") var slotTime: String? = null,
    @SerializedName("speciality") var speciality: String? = null,
    @ColumnInfo("user_uuid") @SerializedName("user_uuid") var userUuid: String? = null,
    @ColumnInfo("dr_name") @SerializedName("dr_name") var drName: String? = null,
    @ColumnInfo("visit_uuid") @SerializedName("visit_uuid") var visitUuid: String? = null,
    @ColumnInfo("patient_name") @SerializedName("patient_name") var patientName: String? = null,
    @ColumnInfo("open_mrs_id") @SerializedName("open_mrs_id") var openMrsId: String? = null,
    @ColumnInfo("patient_id") @SerializedName("patient_id") var patientId: String? = null,
    @SerializedName("status") var status: String? = null,
    @ColumnInfo("created_at") @SerializedName("created_at") var createdAt: String? = null,
    @ColumnInfo("updated_at") @SerializedName("updated_at") var updatedAt: String? = null,
    @ColumnInfo("location_uuid") @SerializedName("location_uuid") var locationUuid: String? = null,
    @ColumnInfo("hw_uuid") @SerializedName("hw_uuid") var hwUuid: String? = null,
    @SerializedName("reason") var reason: String? = null,
    @ColumnInfo("prev_slot_day") @SerializedName("prev_slot_day") var prevDaySlot: String? = null,
    @ColumnInfo("prev_slot_date") @SerializedName("prev_slot_date") var prevDateSlot: String? = null,
    @ColumnInfo("prev_slot_time") @SerializedName("prev_slot_time") var prevTimeSlot: String? = null,
    @SerializedName("voided") var voided: Int = 0,
    @SerializedName("sync") var sync: Boolean = false,
) : Parcelable
