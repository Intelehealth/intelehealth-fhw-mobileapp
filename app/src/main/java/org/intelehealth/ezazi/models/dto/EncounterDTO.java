
package org.intelehealth.ezazi.models.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.intelehealth.ezazi.app.AppConstants;
import org.intelehealth.klivekit.utils.DateTimeUtils;

import java.util.List;
import java.util.TimeZone;

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
    private String encounterTypeName;

    private String providerName;

    private List<ObsDTO> obsDTOList;

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

    public String getEncounterTypeName() {
        return encounterTypeName;
    }

    public void setEncounterTypeName(String encounterTypeName) {
        this.encounterTypeName = encounterTypeName;
    }

    private Type encounterType = Type.NORMAL;

    public void setEncounterType(Type encounterType) {
        this.encounterType = encounterType;
    }

    public Type getEncounterType() {
        return encounterType;
    }

    public String getEncounterDateTime() {
        return DateTimeUtils.formatDate(
                DateTimeUtils.parseUTCDate(encounterTime, AppConstants.UTC_FORMAT),
                DateTimeUtils.DD_MMM_YYYY_HH_MM_A, TimeZone.getDefault());
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public String getProviderName() {
        return providerName;
    }

    public List<ObsDTO> getObsDTOList() {
        return obsDTOList;
    }

    public void setObsDTOList(List<ObsDTO> obsDTOList) {
        this.obsDTOList = obsDTOList;
    }

    public enum Type {SOS, NORMAL}

    public enum Status {MISSED, SUBMITTED, PENDING}
}