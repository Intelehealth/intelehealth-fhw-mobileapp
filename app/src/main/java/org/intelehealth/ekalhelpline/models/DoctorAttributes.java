package org.intelehealth.ekalhelpline.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DoctorAttributes {

    @SerializedName("phoneNumber")
    @Expose
    private String doctorPhoneNo;

    @SerializedName("qualification")
    @Expose
    private String doctorQualification;

    @SerializedName("whatsapp")
    @Expose
    private String doctorWhatsApp;

    @SerializedName("timings")
    @Expose
    private String doctorTimings;

    @SerializedName("specialization")
    @Expose
    private String doctorSpecialization;

    public String getDoctorPhoneNo() {
        return doctorPhoneNo;
    }

    public void setDoctorPhoneNo(String doctorPhoneNo) {
        this.doctorPhoneNo = doctorPhoneNo;
    }

    public String getDoctorQualification() {
        return doctorQualification;
    }

    public void setDoctorQualification(String doctorQualification) {
        this.doctorQualification = doctorQualification;
    }

    public String getDoctorWhatsApp() {
        return doctorWhatsApp;
    }

    public void setDoctorWhatsApp(String doctorWhatsApp) {
        this.doctorWhatsApp = doctorWhatsApp;
    }

    public String getDoctorSpecialization() {
        return doctorSpecialization;
    }

    public void setDoctorSpecialization(String doctorSpecialization) {
        this.doctorSpecialization = doctorSpecialization;
    }

    public String getDoctorTimings() {
        return doctorTimings;
    }

    public void setDoctorTimings(String doctorTimings) {
        this.doctorTimings = doctorTimings;
    }
}
