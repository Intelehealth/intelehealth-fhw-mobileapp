
package io.intelehealth.client.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class EncounterDTO {
    @SerializedName("uuid")
    @Expose
    private String uuid;
    @SerializedName("visituuid")
    @Expose
    private String visituuid;
    @SerializedName("encounter_type_uuid")
    @Expose
    private String encounterTypeUuid;
    @SerializedName("encounter_time")
    @Expose
    private String encounterTime;
    @SerializedName("syncd")
    @Expose
    private Boolean syncd;
    @SerializedName("voided")
    @Expose
    private Integer voided;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getVisituuid() {
        return visituuid;
    }

    public void setVisituuid(String visituuid) {
        this.visituuid = visituuid;
    }

    public String getEncounterTypeUuid() {
        return encounterTypeUuid;
    }

    public void setEncounterTypeUuid(String encounterTypeUuid) {
        this.encounterTypeUuid = encounterTypeUuid;
    }

    public Boolean getSyncd() {
        return syncd;
    }

    public void setSyncd(Boolean syncd) {
        this.syncd = syncd;
    }

    public Integer getVoided() {
        return voided;
    }

    public void setVoided(Integer voided) {
        this.voided = voided;
    }

    public String getEncounterTime() {
        return encounterTime;
    }

    public void setEncounterTime(String encounterTime) {
        this.encounterTime = encounterTime;
    }
}