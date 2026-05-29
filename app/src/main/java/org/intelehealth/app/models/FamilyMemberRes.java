package org.intelehealth.app.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FamilyMemberRes {

    @SerializedName("openMRSID")
    @Expose
    private String openMRSID;
    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("uuid")
    @Expose
    private String uuid;

    private boolean headOfHousehold;

    public String getOpenMRSID() {
        return openMRSID;
    }

    public void setOpenMRSID(String openMRSID) {
        this.openMRSID = openMRSID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public boolean isHeadOfHousehold() {
        return headOfHousehold;
    }

    public void setHeadOfHousehold(boolean headOfHousehold) {
        this.headOfHousehold = headOfHousehold;
    }
}
