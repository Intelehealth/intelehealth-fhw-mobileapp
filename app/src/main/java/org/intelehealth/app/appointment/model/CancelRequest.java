package org.intelehealth.app.appointment.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class CancelRequest implements Serializable {

    @SerializedName("id")
    private int id;

    @SerializedName("visitUuid")
    private String visitUuid;

    @SerializedName("hwUUID")
    private String hwUuid;

    @SerializedName("reason")
    private String reason;


    public String getVisitUuid() {
        return visitUuid;
    }

    public void setVisitUuid(String visitUuid) {
        this.visitUuid = visitUuid;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getHwUuid() {
        return hwUuid;
    }

    public void setHwUuid(String hwUuid) {
        this.hwUuid = hwUuid;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
