package org.intelehealth.ekalarogya.models.UserProfileModel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class HwPersonalInformationModel {
    @SerializedName("Gender")
    @Expose
    private String Gender;

    @SerializedName("State")
    @Expose
    private String State;

    @SerializedName("Mobile")
    @Expose
    private String Mobile;

    @SerializedName("WhatsApp")
    @Expose
    private String WhatsApp;

    @SerializedName("Email")
    @Expose
    private String Email;

    public String getGender() {
        return Gender;
    }

    public void setGender(String Gender) {
        this.Gender = Gender;
    }

    public String getState() {
        return State;
    }

    public void setState(String State) {
        this.State = State;
    }

    public String getMobile() {
        return Mobile;
    }

    public void setMobile(String Mobile) {
        this.Mobile = Mobile;
    }

    public String getWhatsApp() {
        return WhatsApp;
    }

    public void setWhatsApp(String WhatsApp) {
        this.WhatsApp = WhatsApp;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String Gender) {
        this.Email = Email;
    }
}
