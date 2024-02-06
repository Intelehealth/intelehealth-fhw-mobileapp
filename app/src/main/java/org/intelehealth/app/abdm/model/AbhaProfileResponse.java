package org.intelehealth.app.abdm.model;

/**
 * Created by - Prajwal W. on 06/02/24.
 * Email: prajwalwaingankar@gmail.com
 * Mobile: +917304154312
 **/
import java.io.Serializable;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AbhaProfileResponse implements Serializable {

    @SerializedName("ABHANumber")
    @Expose
    private String aBHANumber;
    @SerializedName("preferredAbhaAddress")
    @Expose
    private String preferredAbhaAddress;
    @SerializedName("mobile")
    @Expose
    private String mobile;
    @SerializedName("firstName")
    @Expose
    private String firstName;
    @SerializedName("middleName")
    @Expose
    private String middleName;
    @SerializedName("lastName")
    @Expose
    private String lastName;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("yearOfBirth")
    @Expose
    private String yearOfBirth;
    @SerializedName("dayOfBirth")
    @Expose
    private String dayOfBirth;
    @SerializedName("monthOfBirth")
    @Expose
    private String monthOfBirth;
    @SerializedName("gender")
    @Expose
    private String gender;
    @SerializedName("email")
    @Expose
    private Object email;
    @SerializedName("profilePhoto")
    @Expose
    private String profilePhoto;
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("stateCode")
    @Expose
    private String stateCode;
    @SerializedName("districtCode")
    @Expose
    private String districtCode;
    @SerializedName("subDistrictCode")
    @Expose
    private Object subDistrictCode;
    @SerializedName("villageCode")
    @Expose
    private Object villageCode;
    @SerializedName("townCode")
    @Expose
    private Object townCode;
    @SerializedName("wardCode")
    @Expose
    private Object wardCode;
    @SerializedName("pincode")
    @Expose
    private String pincode;
    @SerializedName("address")
    @Expose
    private String address;
    @SerializedName("kycPhoto")
    @Expose
    private String kycPhoto;
    @SerializedName("stateName")
    @Expose
    private String stateName;
    @SerializedName("districtName")
    @Expose
    private String districtName;
    @SerializedName("subdistrictName")
    @Expose
    private String subdistrictName;
    @SerializedName("villageName")
    @Expose
    private Object villageName;
    @SerializedName("townName")
    @Expose
    private String townName;
    @SerializedName("wardName")
    @Expose
    private Object wardName;
    @SerializedName("authMethods")
    @Expose
    private List<String> authMethods;
    @SerializedName("tags")
    @Expose
    private Tags tags;
    @SerializedName("kycVerified")
    @Expose
    private Boolean kycVerified;
    @SerializedName("verificationStatus")
    @Expose
    private Object verificationStatus;
    @SerializedName("verificationType")
    @Expose
    private Object verificationType;
    @SerializedName("emailVerified")
    @Expose
    private Object emailVerified;

    public String getABHANumber() {
        return aBHANumber;
    }

    public void setABHANumber(String aBHANumber) {
        this.aBHANumber = aBHANumber;
    }

    public String getPreferredAbhaAddress() {
        return preferredAbhaAddress;
    }

    public void setPreferredAbhaAddress(String preferredAbhaAddress) {
        this.preferredAbhaAddress = preferredAbhaAddress;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getYearOfBirth() {
        return yearOfBirth;
    }

    public void setYearOfBirth(String yearOfBirth) {
        this.yearOfBirth = yearOfBirth;
    }

    public String getDayOfBirth() {
        return dayOfBirth;
    }

    public void setDayOfBirth(String dayOfBirth) {
        this.dayOfBirth = dayOfBirth;
    }

    public String getMonthOfBirth() {
        return monthOfBirth;
    }

    public void setMonthOfBirth(String monthOfBirth) {
        this.monthOfBirth = monthOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Object getEmail() {
        return email;
    }

    public void setEmail(Object email) {
        this.email = email;
    }

    public String getProfilePhoto() {
        return profilePhoto;
    }

    public void setProfilePhoto(String profilePhoto) {
        this.profilePhoto = profilePhoto;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStateCode() {
        return stateCode;
    }

    public void setStateCode(String stateCode) {
        this.stateCode = stateCode;
    }

    public String getDistrictCode() {
        return districtCode;
    }

    public void setDistrictCode(String districtCode) {
        this.districtCode = districtCode;
    }

    public Object getSubDistrictCode() {
        return subDistrictCode;
    }

    public void setSubDistrictCode(Object subDistrictCode) {
        this.subDistrictCode = subDistrictCode;
    }

    public Object getVillageCode() {
        return villageCode;
    }

    public void setVillageCode(Object villageCode) {
        this.villageCode = villageCode;
    }

    public Object getTownCode() {
        return townCode;
    }

    public void setTownCode(Object townCode) {
        this.townCode = townCode;
    }

    public Object getWardCode() {
        return wardCode;
    }

    public void setWardCode(Object wardCode) {
        this.wardCode = wardCode;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getKycPhoto() {
        return kycPhoto;
    }

    public void setKycPhoto(String kycPhoto) {
        this.kycPhoto = kycPhoto;
    }

    public String getStateName() {
        return stateName;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }

    public String getDistrictName() {
        return districtName;
    }

    public void setDistrictName(String districtName) {
        this.districtName = districtName;
    }

    public String getSubdistrictName() {
        return subdistrictName;
    }

    public void setSubdistrictName(String subdistrictName) {
        this.subdistrictName = subdistrictName;
    }

    public Object getVillageName() {
        return villageName;
    }

    public void setVillageName(Object villageName) {
        this.villageName = villageName;
    }

    public String getTownName() {
        return townName;
    }

    public void setTownName(String townName) {
        this.townName = townName;
    }

    public Object getWardName() {
        return wardName;
    }

    public void setWardName(Object wardName) {
        this.wardName = wardName;
    }

    public List<String> getAuthMethods() {
        return authMethods;
    }

    public void setAuthMethods(List<String> authMethods) {
        this.authMethods = authMethods;
    }

    public Tags getTags() {
        return tags;
    }

    public void setTags(Tags tags) {
        this.tags = tags;
    }

    public Boolean getKycVerified() {
        return kycVerified;
    }

    public void setKycVerified(Boolean kycVerified) {
        this.kycVerified = kycVerified;
    }

    public Object getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(Object verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public Object getVerificationType() {
        return verificationType;
    }

    public void setVerificationType(Object verificationType) {
        this.verificationType = verificationType;
    }

    public Object getEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(Object emailVerified) {
        this.emailVerified = emailVerified;
    }

    @Override
    public String toString() {
        return "AbhaProfileResponse{" +
                "aBHANumber='" + aBHANumber + '\'' +
                ", preferredAbhaAddress='" + preferredAbhaAddress + '\'' +
                ", mobile='" + mobile + '\'' +
                ", firstName='" + firstName + '\'' +
                ", middleName='" + middleName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", name='" + name + '\'' +
                ", yearOfBirth='" + yearOfBirth + '\'' +
                ", dayOfBirth='" + dayOfBirth + '\'' +
                ", monthOfBirth='" + monthOfBirth + '\'' +
                ", gender='" + gender + '\'' +
                ", email=" + email +
                ", profilePhoto='" + profilePhoto + '\'' +
                ", status='" + status + '\'' +
                ", stateCode='" + stateCode + '\'' +
                ", districtCode='" + districtCode + '\'' +
                ", subDistrictCode=" + subDistrictCode +
                ", villageCode=" + villageCode +
                ", townCode=" + townCode +
                ", wardCode=" + wardCode +
                ", pincode='" + pincode + '\'' +
                ", address='" + address + '\'' +
                ", kycPhoto='" + kycPhoto + '\'' +
                ", stateName='" + stateName + '\'' +
                ", districtName='" + districtName + '\'' +
                ", subdistrictName='" + subdistrictName + '\'' +
                ", villageName=" + villageName +
                ", townName='" + townName + '\'' +
                ", wardName=" + wardName +
                ", authMethods=" + authMethods +
                ", tags=" + tags +
                ", kycVerified=" + kycVerified +
                ", verificationStatus=" + verificationStatus +
                ", verificationType=" + verificationType +
                ", emailVerified=" + emailVerified +
                '}';
    }
}
