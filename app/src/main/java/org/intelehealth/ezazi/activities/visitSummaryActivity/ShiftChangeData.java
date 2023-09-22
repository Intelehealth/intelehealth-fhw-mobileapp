package org.intelehealth.ezazi.activities.visitSummaryActivity;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Kaveri Zaware on 22-09-2023
 * email - kaveri@intelehealth.org
 **/
public class ShiftChangeData {
    @SerializedName("patientNameTimeline")
    private String patientNameTimeline;

    public String getToHwUserUuid() {
        return toHwUserUuid;
    }

    public void setToHwUserUuid(String toHwUserUuid) {
        this.toHwUserUuid = toHwUserUuid;
    }

    @SerializedName("patientUuid")
    private String patientUuid;

    @SerializedName("toHwUserUuid")
    private String toHwUserUuid;


    public String getPatientNameTimeline() {
        return patientNameTimeline;
    }

    public void setPatientNameTimeline(String patientNameTimeline) {
        this.patientNameTimeline = patientNameTimeline;
    }

    public String getPatientUuid() {
        return patientUuid;
    }

    public void setPatientUuid(String patientUuid) {
        this.patientUuid = patientUuid;
    }

    public String getVisitUuid() {
        return visitUuid;
    }

    public void setVisitUuid(String visitUuid) {
        this.visitUuid = visitUuid;
    }

    public String getProviderID() {
        return providerID;
    }

    public void setProviderID(String providerID) {
        this.providerID = providerID;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    @SerializedName("visitUuid")
    private String visitUuid;

    public String getAssignorNurse() {
        return assignorNurse;
    }

    public void setAssignorNurse(String assignorNurse) {
        this.assignorNurse = assignorNurse;
    }

    @SerializedName("providerID")
    private String providerID;

    @SerializedName("tag")
    private String tag;

    @SerializedName("assignorNurse")
    private String assignorNurse;
}
