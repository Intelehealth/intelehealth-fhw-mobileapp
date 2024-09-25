package com.intelehealth.appointment.network.request;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class CancelRequest implements Serializable {

    @SerializedName("id")
    private int id;

    @SerializedName("visitUuid")
    private String visitUuid;

    @SerializedName("hwUUID")
    private String hwUUID;

    @SerializedName("reason")
    private String reason;

    public String getVisitUuid() {
        return visitUuid;
    }

    public void setVisitUuid(String visitUuid) {
        this.visitUuid = visitUuid;
    }

    public int isId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getHwUUID() {
        return hwUUID;
    }

    public void setHwUUID(String hwUUID) {
        this.hwUUID = hwUUID;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}