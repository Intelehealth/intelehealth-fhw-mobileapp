package org.intelehealth.abdm.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LoginOtpVerifyUser {
    @SerializedName("abhaAddress")
    @Expose
    private String abhaAddress;

    @SerializedName("fullName")
    @Expose
    private String fullName;

    @SerializedName("status")
    @Expose
    private String status;

    @SerializedName("kycStatus")
    @Expose
    private String kycStatus;

    @SerializedName("age")
    @Expose
    private int age;

    public String getAbhaAddress() {
        return abhaAddress;
    }

    public void setAbhaAddress(String abhaAddress) {
        this.abhaAddress = abhaAddress;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getKycStatus() {
        return kycStatus;
    }

    public void setKycStatus(String kycStatus) {
        this.kycStatus = kycStatus;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
