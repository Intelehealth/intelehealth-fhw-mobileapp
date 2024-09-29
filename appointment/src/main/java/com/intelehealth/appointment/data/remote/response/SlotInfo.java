package com.intelehealth.appointment.data.remote.response;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class SlotInfo implements Serializable {

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

    private boolean isSelected = false;

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

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

}
