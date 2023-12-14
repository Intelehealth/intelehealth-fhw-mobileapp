package org.intelehealth.kf.models;

import com.google.gson.annotations.SerializedName;

public class ChangePasswordParamsModel_New {
    @SerializedName("newPassword")
    public String newPassword;

    public ChangePasswordParamsModel_New(String newPassword/*, String otp*/) {
        this.newPassword = newPassword;
        /*this.otp = otp;*/
    }

/*    @SerializedName("otp")
      public String otp;*/

}
