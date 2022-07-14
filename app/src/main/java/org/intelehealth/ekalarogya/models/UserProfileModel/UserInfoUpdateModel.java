package org.intelehealth.ekalarogya.models.UserProfileModel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UserInfoUpdateModel {
    @SerializedName("status")
    @Expose
    private boolean status;

    @SerializedName("message")
    @Expose
    private String message;

    @SerializedName("data")
    @Expose
    private UserDataModel userDataModel;;

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public UserDataModel getUserInfoUpdateModel() {
        return userDataModel;
    }

    public void setUserUpdateDataModel(UserDataModel userUpdateModel) {
        this.userDataModel = userDataModel;
    }
}
