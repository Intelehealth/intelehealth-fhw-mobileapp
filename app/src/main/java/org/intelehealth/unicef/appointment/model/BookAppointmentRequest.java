package org.intelehealth.unicef.appointment.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class BookAppointmentRequest implements Serializable {

    @SerializedName("appointmentId")
    private int appointmentId;

    @SerializedName("slotDay")
    private String slotDay;

    @SerializedName("slotDate")
    private String slotDate;

    @SerializedName("slotDuration")
    private int slotDuration;

    @SerializedName("slotDurationUnit")
    private String slotDurationUnit;

    @SerializedName("slotTime")
    private String slotTime;

    @SerializedName("speciality")
    private String speciality;


    @SerializedName("userUuid")
    private String userUuid;

    @SerializedName("drName")
    private String drName;

    @SerializedName("visitUuid")
    private String visitUuid;

    @SerializedName("patientName")
    private String patientName;

    @SerializedName("openMrsId")
    private String openMrsId;

    @SerializedName("patientId")
    private String patientId;

    @SerializedName("locationUuid")
    private String locationUuid;

    @SerializedName("hwUUID")
    private String hwUUID;

    @SerializedName("reason")
    private String reason;


    public String getSlotDay() {
        return slotDay;
    }

    public void setSlotDay(String slotDay) {
        this.slotDay = slotDay;
    }

    public String getSlotDate() {
        return slotDate;
    }

    public void setSlotDate(String slotDate) {
        this.slotDate = slotDate;
    }

    public int getSlotDuration() {
        return slotDuration;
    }

    public void setSlotDuration(int slotDuration) {
        this.slotDuration = slotDuration;
    }

    public String getSlotDurationUnit() {
        return slotDurationUnit;
    }

    public void setSlotDurationUnit(String slotDurationUnit) {
        this.slotDurationUnit = slotDurationUnit;
    }

    public String getSlotTime() {
        return slotTime;
    }

    public void setSlotTime(String slotTime) {
        this.slotTime = slotTime;
    }

    public String getSpeciality() {
        return speciality;
    }

    public void setSpeciality(String speciality) {
        this.speciality = speciality;
    }

    public String getUserUuid() {
        return userUuid;
    }

    public void setUserUuid(String userUuid) {
        this.userUuid = userUuid;
    }

    public String getDrName() {
        return drName;
    }

    public void setDrName(String drName) {
        this.drName = drName;
    }

    public String getVisitUuid() {
        return visitUuid;
    }

    public void setVisitUuid(String visitUuid) {
        this.visitUuid = visitUuid;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getOpenMrsId() {
        return openMrsId;
    }

    public void setOpenMrsId(String openMrsId) {
        this.openMrsId = openMrsId;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }


    public String getLocationUuid() {
        return locationUuid;
    }

    public void setLocationUuid(String locationUuid) {
        this.locationUuid = locationUuid;
    }

    public String getHwUUID() {
        return hwUUID;
    }

    public void setHwUUID(String hwUUID) {
        this.hwUUID = hwUUID;
    }

    public int getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(int appointmentId) {
        this.appointmentId = appointmentId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
