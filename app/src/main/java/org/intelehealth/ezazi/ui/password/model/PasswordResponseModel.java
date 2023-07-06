package org.intelehealth.ezazi.ui.password.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Kaveri Zaware on 04-07-2023
 * email - kaveri@intelehealth.org
 **/
public class PasswordResponseModel {

    @SerializedName("userUuid")
    @Expose
    private String userUuid;
    @SerializedName("providerUuid")
    @Expose
    private String providerUuid;

    public String getUserUuid() {
        return userUuid;
    }

    public void setUserUuid(String userUuid) {
        this.userUuid = userUuid;
    }

    public String getProviderUuid() {
        return providerUuid;
    }

    public void setProviderUuid(String providerUuid) {
        this.providerUuid = providerUuid;
    }
}
