package org.intelehealth.ezazi.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FamilyMemberRes {

    @SerializedName("openMRSID")
    @Expose
    private String openMRSID;
    @SerializedName("name")
    @Expose
    private String name;

    // Added by Mithun Vaghela
    private String visitUuid;

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

    public String getVisitUuid() {
        return visitUuid;
    }

    public void setVisitUuid(String visitUuid) {
        this.visitUuid = visitUuid;
    }
}
