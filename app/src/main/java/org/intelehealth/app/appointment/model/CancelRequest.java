package org.intelehealth.app.appointment.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class CancelRequest implements Serializable {
    public int getId() {
        return id;
    }

    public String getHwUUID() {
        return hwUUID;
    }

    public void setHwUUID(String hwUUID) {
        this.hwUUID = hwUUID;
    }

    @SerializedName("id")
    private int id;

    @SerializedName("visitUuid")
    private String visitUuid;

    @SerializedName("hwUUID")
    private String hwUUID;


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
}
