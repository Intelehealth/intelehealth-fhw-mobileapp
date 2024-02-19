package org.intelehealth.app.models.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ProviderDTO {

    @SerializedName("role")
    @Expose
    private String role;

    public ProviderDTO() {
    }

    public String getMiddle_name() {
        return middle_name;
    }

    public void setMiddle_name(String middle_name) {
        this.middle_name = middle_name;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public ProviderDTO(String role, String useruuid, String emailId, String telephoneNumber, String providerId,
                       String givenName, String familyName, Integer voided, String gender, String dateofbirth,
                       String uuid, String identifier, String countryCode, String middle_name) {
        this.role = role;
        this.useruuid = useruuid;
        this.emailId = emailId;
        this.telephoneNumber = telephoneNumber;
        this.providerId = providerId;
        this.givenName = givenName;
        this.familyName = familyName;
        this.voided = voided;
        this.gender = gender;
        this.dateofbirth = dateofbirth;
        this.uuid = uuid;
        this.countryCode = countryCode;
        this.middle_name = middle_name;
        this.identifier = identifier;

    }

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

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    @SerializedName("dateofbirth")
    @Expose
    private String dateofbirth;
    @SerializedName("uuid")
    @Expose
    private String uuid;
    @SerializedName("identifier")
    @Expose
    private String identifier;

    @SerializedName("middle_name")
    @Expose
    private String middle_name;

    @SerializedName("countryCode")
    @Expose
    private String countryCode;

    String imagePath;

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

    public enum Columns {
        USER_UUID("useruuid"), UUID("uuid");
        public final String value;

        Columns(String value) {
            this.value = value;
        }
    }

}
  /*  @SerializedName("uuid")
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
    @SerializedName("role")
    @Expose
    private String role;
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
*/
