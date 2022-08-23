package org.intelehealth.app.appointment.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class AppointmentInfo implements Serializable {

    @SerializedName("uuid")
    private String uuid;

    @SerializedName("id")
    private int id;

    @SerializedName("slotDay")
    private String slotDay;

    @SerializedName("slotDate")
    private String slotDate;

    @SerializedName("slotJsDate")
    private String slotJsDate;

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

    @SerializedName("status")
    private String status;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("updatedAt")
    private String updatedAt;


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


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSlotJsDate() {
        return slotJsDate;
    }

    public void setSlotJsDate(String slotJsDate) {
        this.slotJsDate = slotJsDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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


    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
