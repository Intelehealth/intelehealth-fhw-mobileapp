package org.intelehealth.ekalarogya.models.UserProfileModel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MainProfileModel {
    @SerializedName("status")
    @Expose
    private boolean status;

    @SerializedName("message")
    @Expose
    private String message;

    @SerializedName("data")
    @Expose
    private HwProfileModel hwProfileModel;;

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

    public HwProfileModel getHwProfileModel() {
        return hwProfileModel;
    }

    public void setHwProfileModel(HwProfileModel hwProfileModel) {
        this.hwProfileModel = hwProfileModel;
    }
}
