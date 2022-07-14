package org.intelehealth.ekalarogya.models.UserProfileModel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UserAtteributModel {
    @SerializedName("emailId")
    @Expose
    private String emailId;

    @SerializedName("timings")
    @Expose
    private String timings;

    @SerializedName("qualification")
    @Expose
    private String qualification;

    @SerializedName("aboutMe")
    @Expose
    private String aboutMe;

    @SerializedName("textOfSign")
    @Expose
    private String textOfSign;

    @SerializedName("fontOfSign")
    @Expose
    private String fontOfSign;

    @SerializedName("registrationNumber")
    @Expose
    private String registrationNumber;

    @SerializedName("phoneNumber")
    @Expose
    private String phoneNumber;

    @SerializedName("whatsapp")
    @Expose
    private String whatsapp;

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getTimings() {
        return timings;
    }

    public void setTimings(String timings) {
        this.timings = timings;
    }

    public String getQualification() {
        return qualification;
    }

    public void setQualification(String qualification) {
        this.qualification = qualification;
    }

    public String getAboutMe() {
        return aboutMe;
    }

    public void setAboutMe(String gender) {
        this.aboutMe = aboutMe;
    }
    public String getTextOfSign() {
        return textOfSign;
    }

    public void setTextOfSign(String textOfSign) {
        this.textOfSign = textOfSign;
    }
    public String getFontOfSign() {
        return fontOfSign;
    }

    public void setFontOfSign(String fontOfSign) {
        this.fontOfSign = fontOfSign;
    }
    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    public String getWhatsapp() {
        return whatsapp;
    }

    public void setWhatsapp(String whatsapp) {
        this.whatsapp = whatsapp;
    }
}
