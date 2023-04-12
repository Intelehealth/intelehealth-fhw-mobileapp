package org.intelehealth.app.models;

import com.google.gson.annotations.SerializedName;

public class OTPVerificationParamsModel_New {

    @SerializedName("verifyFor")
    public String otpFor;

    @SerializedName("username")
    public String userName;

    @SerializedName("phoneNumber")
    public String phoneNumber;

    @SerializedName("countryCode")
    public int countryCode;

    @SerializedName("email")
    public String email;

    @SerializedName("otp")
    public String otp;

    public OTPVerificationParamsModel_New(String otpFor, String userName, String phoneNumber, int countryCode, String email, String otp) {
        this.otpFor = otpFor;
        this.userName = userName;
        this.phoneNumber = phoneNumber;
        this.countryCode = countryCode;
        this.email = email;
        this.otp = otp;
    }
}
