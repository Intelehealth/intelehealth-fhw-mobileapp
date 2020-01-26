
package app.intelehealth.client.models.dto;

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
    @SerializedName("provider_uuid")
    @Expose
    private String provideruuid;
    @SerializedName("syncd")
    @Expose
    private Boolean syncd;
    @SerializedName("voided")
    @Expose
    private Integer voided;
    @SerializedName("privacynotice_value")
    @Expose
    private String privacynotice_value;



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

    public String getProvideruuid() {
        return provideruuid;
    }

    public void setProvideruuid(String provideruuid) {
        this.provideruuid = provideruuid;
    }

    public String getPrivacynotice_value() {
        return privacynotice_value;
    }

    public void setPrivacynotice_value(String privacynotice_value) {
        this.privacynotice_value = privacynotice_value;
    }
}