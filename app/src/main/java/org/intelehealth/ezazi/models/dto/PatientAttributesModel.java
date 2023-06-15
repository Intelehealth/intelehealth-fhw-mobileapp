package org.intelehealth.ezazi.models.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by Kaveri Zaware on 14-06-2023
 * email - kaveri@intelehealth.org
 **/
public class PatientAttributesModel implements Serializable {

    public boolean isMembraneCheckboxChecked() {
        return isMembraneCheckboxChecked;
    }


    @SerializedName("admissionDate")
    @Expose
    private String admissionDate;
    @SerializedName("admissionTime")
    @Expose
    private String admissionTime;

    @SerializedName("totalMiscarriageCount")
    @Expose
    private String totalMiscarriageCount;

    @SerializedName("totalBirthCount")
    @Expose
    private String totalBirthCount;

    @SerializedName("labourOnset")
    @Expose
    private String labourOnset;

    @SerializedName("activeLabourDiagnosedDate")
    @Expose
    private String activeLabourDiagnosedDate;
    @SerializedName("activeLabourDiagnosedTime")
    @Expose
    private String activeLabourDiagnosedTime;

    @SerializedName("isMembraneCheckboxChecked")
    @Expose
    private boolean isMembraneCheckboxChecked;

    @SerializedName("sacRupturedDate")
    @Expose
    private String sacRupturedDate;

    @SerializedName("sacRupturedTime")
    @Expose
    private String sacRupturedTime;

    @SerializedName("riskFactors")
    @Expose
    private String riskFactors;

    @SerializedName("hospitalMaternity")
    @Expose
    private String hospitalMaternity;

    @SerializedName("primaryDoctor")
    @Expose
    private String primaryDoctor;

    @SerializedName("secondaryDoctor")
    @Expose
    private String secondaryDoctor;

    @SerializedName("bedNumber")
    @Expose
    private String bedNumber;


    public String getOtherHospitalString() {
        return otherHospitalString;
    }

    public void setOtherHospitalString(String otherHospitalString) {
        this.otherHospitalString = otherHospitalString;
    }

    String otherHospitalString;
    public String getAdmissionDate() {
        return admissionDate;
    }

    public void setAdmissionDate(String admissionDate) {
        this.admissionDate = admissionDate;
    }

    public String getAdmissionTime() {
        return admissionTime;
    }

    public void setAdmissionTime(String admissionTime) {
        this.admissionTime = admissionTime;
    }

    public String getTotalMiscarriageCount() {
        return totalMiscarriageCount;
    }

    public void setTotalMiscarriageCount(String totalMiscarriageCount) {
        this.totalMiscarriageCount = totalMiscarriageCount;
    }

    public String getTotalBirthCount() {
        return totalBirthCount;
    }

    public void setTotalBirthCount(String totalBirthCount) {
        this.totalBirthCount = totalBirthCount;
    }

    public String getLabourOnset() {
        return labourOnset;
    }

    public void setLabourOnset(String labourOnset) {
        this.labourOnset = labourOnset;
    }

    public String getActiveLabourDiagnosedDate() {
        return activeLabourDiagnosedDate;
    }

    public void setActiveLabourDiagnosedDate(String activeLabourDiagnosedDate) {
        this.activeLabourDiagnosedDate = activeLabourDiagnosedDate;
    }

    public String getActiveLabourDiagnosedTime() {
        return activeLabourDiagnosedTime;
    }

    public void setActiveLabourDiagnosedTime(String activeLabourDiagnosedTime) {
        this.activeLabourDiagnosedTime = activeLabourDiagnosedTime;
    }


    public void setMembraneCheckboxChecked(boolean membraneCheckboxChecked) {
        isMembraneCheckboxChecked = membraneCheckboxChecked;
    }

    public String getSacRupturedDate() {
        return sacRupturedDate;
    }

    public void setSacRupturedDate(String sacRupturedDate) {
        this.sacRupturedDate = sacRupturedDate;
    }

    public String getSacRupturedTime() {
        return sacRupturedTime;
    }

    public void setSacRupturedTime(String sacRupturedTime) {
        this.sacRupturedTime = sacRupturedTime;
    }

    public String getRiskFactors() {
        return riskFactors;
    }

    public void setRiskFactors(String riskFactors) {
        this.riskFactors = riskFactors;
    }

    public String getHospitalMaternity() {
        return hospitalMaternity;
    }

    public void setHospitalMaternity(String hospitalMaternity) {
        this.hospitalMaternity = hospitalMaternity;
    }

    public String getPrimaryDoctor() {
        return primaryDoctor;
    }

    public void setPrimaryDoctor(String primaryDoctor) {
        this.primaryDoctor = primaryDoctor;
    }

    public String getSecondaryDoctor() {
        return secondaryDoctor;
    }

    public void setSecondaryDoctor(String secondaryDoctor) {
        this.secondaryDoctor = secondaryDoctor;
    }

    public String getBedNumber() {
        return bedNumber;
    }

    public void setBedNumber(String bedNumber) {
        this.bedNumber = bedNumber;
    }
}
