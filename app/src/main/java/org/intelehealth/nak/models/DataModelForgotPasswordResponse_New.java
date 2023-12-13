package org.intelehealth.nak.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DataModelForgotPasswordResponse_New {

    /*@SerializedName("username")
    @Expose
    private String username;
    @SerializedName("system_id")
    @Expose
    private String systemId;*/
    @SerializedName("userUuid")
    @Expose
    private String uuid;
    @SerializedName("providerUuid")
    @Expose
    private String personId;

    /*public String getUsername() {
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
    }*/

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getPersonId() {
        return personId;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

}
