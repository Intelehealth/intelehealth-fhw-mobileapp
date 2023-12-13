package org.intelehealth.nak.models;

import com.google.gson.annotations.SerializedName;

public class RequestOTPParamsModel_New {

    @SerializedName("otpFor")
    public String otpFor;

    @SerializedName("username")
    public String userName;

    @SerializedName("phoneNumber")
    public String phoneNumber;

    @SerializedName("countryCode")
    public int countryCode;

    @SerializedName("email")
    public String email;

    public RequestOTPParamsModel_New(String otpFor, String userName, String phoneNumber, int countryCode, String email) {
        this.otpFor = otpFor;
        this.userName = userName;
        this.phoneNumber = phoneNumber;
        this.countryCode = countryCode;
        this.email = email;
    }
}
