package org.intelehealth.ezazi.ui.password.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Kaveri Zaware on 05-07-2023
 * email - kaveri@intelehealth.org
 **/
public class VerifyOtpRequestModel {
    @SerializedName("verifyFor")
    @Expose
    private String verifyFor;

    public void setOtpFor(String verifyFor) {
        this.verifyFor = verifyFor;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    @SerializedName("phoneNumber")
    @Expose
    private String phoneNumber;

    public VerifyOtpRequestModel() {
    }

    public VerifyOtpRequestModel(String verifyFor, String phoneNumber, String countryCode, String otp) {
        this.verifyFor = verifyFor;
        this.phoneNumber = phoneNumber;
        this.countryCode = countryCode;
        this.otp = otp;
    }

    @SerializedName("countryCode")
    @Expose
    private String countryCode;

    @SerializedName("otp")
    @Expose
    private String otp;

}
