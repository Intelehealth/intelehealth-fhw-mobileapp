package org.intelehealth.kf.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DataModelForgotPasswordResponse {

    @SerializedName("username")
    @Expose
    private String username;
    @SerializedName("system_id")
    @Expose
    private String systemId;
    @SerializedName("uuid")
    @Expose
    private String uuid;
    @SerializedName("person_id")
    @Expose
    private Integer personId;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSystemId() {
        return systemId;
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Integer getPersonId() {
        return personId;
    }

    public void setPersonId(Integer personId) {
        this.personId = personId;
    }

}
