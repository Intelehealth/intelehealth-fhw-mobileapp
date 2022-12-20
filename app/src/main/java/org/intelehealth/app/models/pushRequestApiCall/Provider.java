package org.intelehealth.app.models.pushRequestApiCall;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Provider {

    @SerializedName("useruuid")
    @Expose
    private String useruuid;
    @SerializedName("emailId")
    @Expose
    private String emailId;
    @SerializedName("telephoneNumber")
    @Expose
    private String telephoneNumber;
    @SerializedName("providerId")
    @Expose
    private String providerId;
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
    @SerializedName("voided")
    @Expose
    private Integer voided;
    @SerializedName("gender")
    @Expose
    private String gender;
    @SerializedName("dateofbirth")
    @Expose
    private String dateofbirth;

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getMiddle_name() {
        return middle_name;
    }

    public void setMiddle_name(String middle_name) {
        this.middle_name = middle_name;
    }

    @SerializedName("countryCode")
    @Expose
    private String countryCode;
    @SerializedName("middle_name")
    @Expose
    private String middle_name;

    public String getUseruuid() {
        return useruuid;
    }

    public void setUseruuid(String useruuid) {
        this.useruuid = useruuid;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getTelephoneNumber() {
        return telephoneNumber;
    }

    public void setTelephoneNumber(String telephoneNumber) {
        this.telephoneNumber = telephoneNumber;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

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

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDateofbirth() {
        return dateofbirth;
    }

    public void setDateofbirth(String dateofbirth) {
        this.dateofbirth = dateofbirth;
    }

}
