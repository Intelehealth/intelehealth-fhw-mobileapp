package org.intelehealth.app.models.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ProviderDTO {
    @SerializedName("uuid")
    @Expose
    private String uuid;
    @SerializedName("identifier")
    @Expose
    private String identifier;
    @SerializedName("given_name")
    @Expose
    private String givenName;
    @SerializedName("family_name")
    @Expose
    private String familyName;
    @SerializedName("role")
    @Expose
    private String role;
    @SerializedName("useruuid")
    @Expose
    private String useruuid;
    @SerializedName("voided")
    @Expose
    private Integer voided;

    @SerializedName("userid")
    private Integer userId;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public Integer getVoided() {
        return voided;
    }

    public void setVoided(Integer voided) {
        this.voided = voided;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getUseruuid() {
        return useruuid;
    }

    public void setUseruuid(String useruuid) {
        this.useruuid = useruuid;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }
}