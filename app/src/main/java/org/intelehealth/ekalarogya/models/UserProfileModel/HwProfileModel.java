package org.intelehealth.ekalarogya.models.UserProfileModel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class HwProfileModel {
    @SerializedName("userName")
    @Expose
    private String userName;

    /*@SerializedName("Image")
    @Expose
    private String Image;*/

    @SerializedName("Designation")
    @Expose
    private String Designation;

    @SerializedName("AboutMe")
    @Expose
    private String AboutMe;

    @SerializedName("patientRegistered")
    @Expose
    private int patientRegistered;

    @SerializedName("visitInProgress")
    @Expose
    private int visitInProgress;

    @SerializedName("CompletedConsultation")
    @Expose
    private int CompletedConsultation;

    @SerializedName("personalInformation")
    @Expose
    private HwPersonalInformationModel personalInformation;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

   /* public String getImage() {
        return Image;
    }

    public void setImage(String Image) {
        this.Image = Image;
    }*/

    public String getDesignation() {
        return Designation;
    }

    public void setDesignation(String Designation) {
        this.Designation = Designation;
    }

    public String getAboutMe() {
        return AboutMe;
    }

    public void setAboutMe(String AboutMe) {
        this.AboutMe = AboutMe;
    }

    public int getPatientRegistered() {
        return patientRegistered;
    }

    public void setPatientRegistered(int patientRegistered) {
        this.patientRegistered = patientRegistered;
    }

    public int getVisitInProgress() {
        return visitInProgress;
    }

    public void setVisitInProgress(int visitInProgress) {
        this.visitInProgress = visitInProgress;
    }

    public int getCompletedConsultation() {
        return CompletedConsultation;
    }

    public void setCompletedConsultation(int CompletedConsultation) {
        this.CompletedConsultation = CompletedConsultation;
    }

    public HwPersonalInformationModel getPersonalInformation() {
        return personalInformation;
    }

    public void setPersonalInformation(HwPersonalInformationModel personalInformation) {
        this.personalInformation = personalInformation;
    }
}
