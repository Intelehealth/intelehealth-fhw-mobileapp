package org.intelehealth.ekalhelpline.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DoctorDetailsModel {

    @SerializedName("givenName")
    @Expose
    private String doctorName;

    @SerializedName("gender")
    @Expose
    private String doctorGender;

    @SerializedName("status")
    @Expose
    private String doctorStatus;

    @SerializedName("attributes")
    @Expose
    public DoctorAttributes doctorAtributesList;

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getDoctorGender() {
        return doctorGender;
    }

    public void setDoctorGender(String doctorGender) {
        this.doctorGender = doctorGender;
    }

    public DoctorAttributes getDoctorAtributesList() {
        return doctorAtributesList;
    }

    public void setDoctorAtributesList(DoctorAttributes doctorAtributesList) {
        this.doctorAtributesList = doctorAtributesList;
    }

    public String getDoctorStatus() {
        return doctorStatus;
    }

    public void setDoctorStatus(String doctorStatus) {
        this.doctorStatus = doctorStatus;
    }
}
