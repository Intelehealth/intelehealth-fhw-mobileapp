package org.intelehealth.nak.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RescheduledAppointmentsModel {
    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("slotDay")
    @Expose
    private String slotDay;
    @SerializedName("slotDate")
    @Expose
    private String slotDate;
    @SerializedName("slotJsDate")
    @Expose
    private String slotJsDate;
    @SerializedName("slotDuration")
    @Expose
    private Integer slotDuration;
    @SerializedName("slotDurationUnit")
    @Expose
    private String slotDurationUnit;
    @SerializedName("slotTime")
    @Expose
    private String slotTime;
    @SerializedName("speciality")
    @Expose
    private String speciality;
    @SerializedName("userUuid")
    @Expose
    private String userUuid;
    @SerializedName("drName")
    @Expose
    private String drName;
    @SerializedName("visitUuid")
    @Expose
    private String visitUuid;
    @SerializedName("patientId")
    @Expose
    private String patientId;
    @SerializedName("locationUuid")
    @Expose
    private String locationUuid;
    @SerializedName("hwUUID")
    @Expose
    private String hwUUID;
    @SerializedName("patientName")
    @Expose
    private String patientName;
    @SerializedName("openMrsId")
    @Expose
    private String openMrsId;
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("createdBy")
    @Expose
    private String createdBy;
    @SerializedName("updatedBy")
    @Expose
    private Object updatedBy;
    @SerializedName("reason")
    @Expose
    private String reason;
    @SerializedName("createdAt")
    @Expose
    private String createdAt;
    @SerializedName("updatedAt")
    @Expose
    private String updatedAt;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

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

    public String getSlotJsDate() {
        return slotJsDate;
    }

    public void setSlotJsDate(String slotJsDate) {
        this.slotJsDate = slotJsDate;
    }

    public Integer getSlotDuration() {
        return slotDuration;
    }

    public void setSlotDuration(Integer slotDuration) {
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Object getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(Object updatedBy) {
        this.updatedBy = updatedBy;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

}

