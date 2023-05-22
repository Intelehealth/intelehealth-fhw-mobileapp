package org.intelehealth.unicef.appointment.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.intelehealth.unicef.models.RescheduledAppointmentsModel;

import java.io.Serializable;
import java.util.List;

public class AppointmentInfo implements Serializable {
    @Expose
    @SerializedName("uuid")
    private String uuid;
    @Expose
    @SerializedName("id")
    private int id;
    @Expose
    @SerializedName("slotDay")
    private String slotDay;
    @Expose
    @SerializedName("slotDate")
    private String slotDate;
    @Expose
    @SerializedName("slotJsDate")
    private String slotJsDate;
    @Expose
    @SerializedName("slotDuration")
    private int slotDuration;
    @Expose
    @SerializedName("slotDurationUnit")
    private String slotDurationUnit;
    @Expose
    @SerializedName("slotTime")
    private String slotTime;
    @Expose
    @SerializedName("speciality")
    private String speciality;

    @Expose
    @SerializedName("userUuid")
    private String userUuid;
    @Expose
    @SerializedName("drName")
    private String drName;
    @Expose
    @SerializedName("visitUuid")
    private String visitUuid;
    @Expose
    @SerializedName("patientName")
    private String patientName;
    @Expose
    @SerializedName("openMrsId")
    private String openMrsId;
    @Expose
    @SerializedName("patientId")
    private String patientId;
    @Expose
    @SerializedName("status")
    private String status;
    @Expose
    @SerializedName("createdAt")
    private String createdAt;


    public List<RescheduledAppointmentsModel> getRescheduledAppointments() {
        return rescheduledAppointments;
    }

    public void setRescheduledAppointments(List<RescheduledAppointmentsModel> rescheduledAppointments) {
        this.rescheduledAppointments = rescheduledAppointments;
    }

    @Expose
    @SerializedName("updatedAt")
    private String updatedAt;

    //this parameter added for rescheduledAppointments
    @SerializedName("rescheduledAppointments")
    @Expose
    private List<RescheduledAppointmentsModel> rescheduledAppointments = null;

    private boolean prescription_exists = false;
    //newly added for appointment
    private String presc_received_time;
    private String patientProfilePhoto;

    public String getPrev_slot_day() {
        return prev_slot_day;
    }

    public void setPrev_slot_day(String prev_slot_day) {
        this.prev_slot_day = prev_slot_day;
    }

    public String getPrev_slot_date() {
        return prev_slot_date;
    }

    public void setPrev_slot_date(String prev_slot_date) {
        this.prev_slot_date = prev_slot_date;
    }

    public String getPrev_slot_time() {
        return prev_slot_time;
    }

    public void setPrev_slot_time(String prev_slot_time) {
        this.prev_slot_time = prev_slot_time;
    }

    String prev_slot_day, prev_slot_date, prev_slot_time;

    public String getPatientProfilePhoto() {
        return patientProfilePhoto;
    }

    public void setPatientProfilePhoto(String patientProfilePhoto) {
        this.patientProfilePhoto = patientProfilePhoto;
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


    public String getPresc_received_time() {
        return presc_received_time;
    }

    public void setPresc_received_time(String presc_received_time) {
        this.presc_received_time = presc_received_time;
    }

    public boolean isPrescription_exists() {
        return prescription_exists;
    }

    public void setPrescription_exists(boolean prescription_exists) {
        this.prescription_exists = prescription_exists;
    }
}
