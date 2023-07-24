
package org.intelehealth.ezazi.models.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RTCConnectionDTO {
    @SerializedName("uuid")
    @Expose
    private String uuid;
    @SerializedName("visit_uuid")
    @Expose
    private String visitUUID;
    @SerializedName("connection_info")
    @Expose
    private String connectionInfo;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }


    public String getConnectionInfo() {
        return connectionInfo;
    }

    public void setConnectionInfo(String connectionInfo) {
        this.connectionInfo = connectionInfo;
    }

    public String getVisitUUID() {
        return visitUUID;
    }

    public void setVisitUUID(String visitUUID) {
        this.visitUUID = visitUUID;
    }
}