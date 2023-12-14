package org.intelehealth.kf.models.pushResponseApiCall;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AppointmentList {

    @SerializedName("uuid")
    private String uuid;

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

    @Expose
    @SerializedName("patientAge")
    private String patientAge;

    @Expose
    @SerializedName("patientGender")
    private String patientGender;

    @Expose
    @SerializedName("patientPic")
    private String patientPic;

    @Expose
    @SerializedName("hwName")
    private String hwName;

    @Expose
    @SerializedName("hwAge")
    private String hwAge;

    @Expose
    @SerializedName("hwGender")
    private String hwGender;

    @Expose
    @SerializedName("voided")
    private String voided;

    @Expose
    @SerializedName("syncd")
    private String sync;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(int appointmentId) {
        this.appointmentId = appointmentId;
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

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getPatientAge() {
        return patientAge;
    }

    public void setPatientAge(String patientAge) {
        this.patientAge = patientAge;
    }

    public String getPatientGender() {
        return patientGender;
    }

    public void setPatientGender(String patientGender) {
        this.patientGender = patientGender;
    }

    public String getPatientPic() {
        return patientPic;
    }

    public void setPatientPic(String patientPic) {
        this.patientPic = patientPic;
    }

    public String getHwName() {
        return hwName;
    }

    public void setHwName(String hwName) {
        this.hwName = hwName;
    }

    public String getHwAge() {
        return hwAge;
    }

    public void setHwAge(String hwAge) {
        this.hwAge = hwAge;
    }

    public String getHwGender() {
        return hwGender;
    }

    public void setHwGender(String hwGender) {
        this.hwGender = hwGender;
    }

    public String getVoided() {
        return voided;
    }

    public void setVoided(String voided) {
        this.voided = voided;
    }

    public String getSync() {
        return sync;
    }

    public void setSync(String sync) {
        this.sync = sync;
    }
}