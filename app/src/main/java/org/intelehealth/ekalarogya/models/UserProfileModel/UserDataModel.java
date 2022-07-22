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
    private UserAttributeModel userAttributeModel;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public UserAttributeModel getUserAtteributModel() {
        return userAttributeModel;
    }

    public void setUserAtteributModel(UserAttributeModel userAttributeModel) {
        this.userAttributeModel = userAttributeModel;
    }

    public UserPersonModel getUserDataModel() {
        return userPersonModel;
    }

    public void setUserDataModel(UserPersonModel userPersonModel) {
        this.userPersonModel = userPersonModel;
    }
}
