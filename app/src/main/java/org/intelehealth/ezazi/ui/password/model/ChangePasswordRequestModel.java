package org.intelehealth.ezazi.ui.password.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Kaveri Zaware on 06-07-2023
 * email - kaveri@intelehealth.org
 **/
public class ChangePasswordRequestModel {
    public ChangePasswordRequestModel(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    @SerializedName("newPassword")
    private String newPassword;
}
