package org.intelehealth.app.abdm.model;

/**
 * Created by - Prajwal W. on 06/02/24.
 * Email: prajwalwaingankar@gmail.com
 * Mobile: +917304154312
 **/

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Account implements Serializable {

    @SerializedName("ABHANumber")
    @Expose
    private String aBHANumber;
    @SerializedName("preferredAbhaAddress")
    @Expose
    private String preferredAbhaAddress;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("gender")
    @Expose
    private String gender;
    @SerializedName("dob")
    @Expose
    private String dob;
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("profilePhoto")
    @Expose
    private String profilePhoto;
    @SerializedName("kycVerified")
    @Expose
    private Boolean kycVerified;

    public String getABHANumber() {
        return aBHANumber;
    }

    public void setABHANumber(String aBHANumber) {
        this.aBHANumber = aBHANumber;
    }

    public String getPreferredAbhaAddress() {
        return preferredAbhaAddress;
    }

    public void setPreferredAbhaAddress(String preferredAbhaAddress) {
        this.preferredAbhaAddress = preferredAbhaAddress;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProfilePhoto() {
        return profilePhoto;
    }

    public void setProfilePhoto(String profilePhoto) {
        this.profilePhoto = profilePhoto;
    }

    public Boolean getKycVerified() {
        return kycVerified;
    }

    public void setKycVerified(Boolean kycVerified) {
        this.kycVerified = kycVerified;
    }

    @Override
    public String toString() {
        return "Account{" +
                "aBHANumber='" + aBHANumber + '\'' +
                ", preferredAbhaAddress='" + preferredAbhaAddress + '\'' +
                ", name='" + name + '\'' +
                ", gender='" + gender + '\'' +
                ", dob='" + dob + '\'' +
                ", status='" + status + '\'' +
                ", profilePhoto='" + profilePhoto + '\'' +
                ", kycVerified=" + kycVerified +
                '}';
    }
}
