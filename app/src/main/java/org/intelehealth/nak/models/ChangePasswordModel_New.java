package org.intelehealth.nak.models;

import com.google.gson.annotations.SerializedName;

public class ChangePasswordModel_New {
    @SerializedName("oldPassword")
    public String oldPassword;

    public ChangePasswordModel_New(String oldPassword, String newPassword) {
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }

    @SerializedName("newPassword")
    public String newPassword;



}
