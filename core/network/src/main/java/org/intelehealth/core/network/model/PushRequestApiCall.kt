package org.intelehealth.core.network.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import org.intelehealth.coreroomdb.entity.*

/**
 * Created by - Prajwal W. on 14/10/24.
 * Email: prajwalwaingankar@gmail.com
 * Mobile: +917304154312
 **/
data class PushRequestApiCall(
    @SerializedName("appointments")
    @Expose
    private var appointments: List<BookAppointmentRequest>? = null,

    @SerializedName("persons")
    @Expose
    private var persons: List<Person>? = null,

    @SerializedName("patients")
    @Expose
    private var patients: List<Patient>? = null,

    @SerializedName("visits")
    @Expose
    private var visits: List<Visit>? = null,

    @SerializedName("encounters")
    @Expose
    private var encounters: List<Encounter>? = null,

    @SerializedName("providers")
    @Expose
    private var providers: List<Provider>? = null
)

data class BookAppointmentRequest(
    @SerializedName("uuid")
    private var uuid: String? = null,

    @SerializedName("appointmentId")
    private var appointmentId: Int? = null,

    @SerializedName("slotDay")
    private var slotDay: String? = null,

    @SerializedName("slotDate")
    private var slotDate: String?,

    @SerializedName("slotDuration")
    private var slotDuration: Int? = null,

    @SerializedName("slotDurationUnit")
    private var slotDurationUnit: String? = null,

    @SerializedName("slotTime")
    private var slotTime: String? = null,

    @SerializedName("speciality")
    private var speciality: String? = null,

    @SerializedName("userUuid")
    private var userUuid: String? = null,

    @SerializedName("drName")
    private var drName: String? = null,

    @SerializedName("visitUuid")
    private var visitUuid: String? = null,

    @SerializedName("patientName")
    private var patientName: String? = null,

    @SerializedName("openMrsId")
    private var openMrsId: String? = null,

    @SerializedName("patientId")
    private var patientId: String? = null,

    @SerializedName("locationUuid")
    private var locationUuid: String? = null,

    @SerializedName("hwUUID")
    private var hwUUID: String? = null,

    @SerializedName("reason")
    private var reason: String? = null,

    @SerializedName("patientAge")
    @Expose
    private var patientAge: String? = null,

    @SerializedName("patientGender")
    @Expose
    private var patientGender: String? = null,

    @SerializedName("patientPic")
    @Expose
    private var patientPic: String? = null,

    @SerializedName("hwName")
    @Expose
    private var hwName: String? = null,

    @SerializedName("hwAge")
    @Expose
    private var hwAge: String? = null,

    @SerializedName("hwGender")
    @Expose
    private var hwGender: String? = null,

    @SerializedName("voided")
    @Expose
    private var voided: String? = null,

    @SerializedName("sync")
    @Expose
    private var sync: String? = null
)