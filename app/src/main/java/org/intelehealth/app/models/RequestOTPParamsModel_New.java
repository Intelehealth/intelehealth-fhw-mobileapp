package org.intelehealth.app.models;

import com.google.gson.annotations.SerializedName;

public class RequestOTPParamsModel_New {
    @SerializedName("userName")
    public String userName;

    public RequestOTPParamsModel_New(String userName, String phoneNumber) {
        this.userName = userName;
        this.phoneNumber = phoneNumber;
    }

    @SerializedName("phoneNumber")
    public String phoneNumber;

}
