package org.intelehealth.ekalarogya.models.UserProfileModel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UserDataModel {
    @SerializedName("uuid")
    @Expose
    private String uuid;

    @SerializedName("person")
    @Expose
    private UserPersonModel userPersonModel;

    @SerializedName("attributes")
    @Expose
    private UserAtteributModel userAtteributModel;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public UserAtteributModel getUserAtteributModel() {
        return userAtteributModel;
    }

    public void setUserAtteributModel(UserAtteributModel userAtteributModel) {
        this.userAtteributModel = userAtteributModel;
    }

    public UserPersonModel getUserDataModel() {
        return userPersonModel;
    }

    public void setUserDataModel(UserPersonModel userPersonModel) {
        this.userPersonModel = userPersonModel;
    }
}
