package org.intelehealth.ezazi.ui.password.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by Kaveri Zaware on 03-07-2023
 * email - kaveri@intelehealth.org
 **/
public class RequestOTPModel implements Serializable {
    public String getOtpFor() {
        return otpFor;
    }

    public RequestOTPModel(String otpFor, String phoneNumber, String countryCode) {
        this.otpFor = otpFor;
        this.phoneNumber = phoneNumber;
        this.countryCode = countryCode;
    }

    public RequestOTPModel() {
    }

    public RequestOTPModel(String otpFor) {
        this.otpFor = otpFor;
    }

    public void setOtpFor(String otpFor) {
        this.otpFor = otpFor;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    @SerializedName("otpFor")
    @Expose
    private String otpFor;

    @SerializedName("phoneNumber")
    @Expose
    private String phoneNumber;

    @SerializedName("countryCode")
    @Expose
    private String countryCode;
}
