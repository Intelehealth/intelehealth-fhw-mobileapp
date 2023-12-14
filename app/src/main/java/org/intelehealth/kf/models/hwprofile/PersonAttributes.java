package org.intelehealth.kf.models.hwprofile;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.intelehealth.kf.models.loginModel.Role;

public class PersonAttributes {

    @SerializedName("uuid")
    @Expose
    private String uuid;
    @SerializedName("display")
    @Expose
    private String display;
    @SerializedName("value")
    @Expose
    private String value;
    @SerializedName("attributeType")
    @Expose
    private Role attributeTpe;
    @SerializedName("voided")
    @Expose
    private boolean voided;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Role getAttributeTpe() {
        return attributeTpe;
    }

    public void setAttributeTpe(Role attributeTpe) {
        this.attributeTpe = attributeTpe;
    }

    public boolean isVoided() {
        return voided;
    }

    public void setVoided(boolean voided) {
        this.voided = voided;
    }
}
