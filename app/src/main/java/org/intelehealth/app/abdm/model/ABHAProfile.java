package org.intelehealth.app.abdm.model;

/**
 * Created by - Prajwal W. on 01/02/24.
 * Email: prajwalwaingankar@gmail.com
 * Mobile: +917304154312
 **/
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ABHAProfile implements Serializable {

    @SerializedName("firstName")
    @Expose
    private String firstName;
    @SerializedName("middleName")
    @Expose
    private String middleName;
    @SerializedName("lastName")
    @Expose
    private String lastName;
    @SerializedName("dob")
    @Expose
    private String dob;
    @SerializedName("gender")
    @Expose
    private String gender;
    @SerializedName("photo")
    @Expose
    private String photo;
    @SerializedName("mobile")
    @Expose
    private String mobile;
    @SerializedName("email")
    @Expose
    private Object email;
    @SerializedName("phrAddress")
    @Expose
    private List<String> phrAddress;
    @SerializedName("address")
    @Expose
    private String address;
    @SerializedName("districtCode")
    @Expose
    private String districtCode;
    @SerializedName("stateCode")
    @Expose
    private String stateCode;
    @SerializedName("pinCode")
    @Expose
    private String pinCode;
    @SerializedName("abhaType")
    @Expose
    private Object abhaType;
    @SerializedName("stateName")
    @Expose
    private String stateName;
    @SerializedName("districtName")
    @Expose
    private String districtName;
    @SerializedName("ABHANumber")
    @Expose
    private String aBHANumber;
    @SerializedName("abhaStatus")
    @Expose
    private String abhaStatus;

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

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public Object getEmail() {
        return email;
    }

    public void setEmail(Object email) {
        this.email = email;
    }

    public List<String> getPhrAddress() {
        return phrAddress;
    }

    public void setPhrAddress(List<String> phrAddress) {
        this.phrAddress = phrAddress;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDistrictCode() {
        return districtCode;
    }

    public void setDistrictCode(String districtCode) {
        this.districtCode = districtCode;
    }

    public String getStateCode() {
        return stateCode;
    }

    public void setStateCode(String stateCode) {
        this.stateCode = stateCode;
    }

    public String getPinCode() {
        return pinCode;
    }

    public void setPinCode(String pinCode) {
        this.pinCode = pinCode;
    }

    public Object getAbhaType() {
        return abhaType;
    }

    public void setAbhaType(Object abhaType) {
        this.abhaType = abhaType;
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

    public String getABHANumber() {
        return aBHANumber;
    }

    public void setABHANumber(String aBHANumber) {
        this.aBHANumber = aBHANumber;
    }

    public String getAbhaStatus() {
        return abhaStatus;
    }

    public void setAbhaStatus(String abhaStatus) {
        this.abhaStatus = abhaStatus;
    }

    @Override
    public String toString() {
        return "ABHAProfile{" +
                "firstName='" + firstName + '\'' +
                ", middleName='" + middleName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", dob='" + dob + '\'' +
                ", gender='" + gender + '\'' +
                ", photo='" + photo + '\'' +
                ", mobile='" + mobile + '\'' +
                ", email=" + email +
                ", phrAddress=" + phrAddress +
                ", address='" + address + '\'' +
                ", districtCode='" + districtCode + '\'' +
                ", stateCode='" + stateCode + '\'' +
                ", pinCode='" + pinCode + '\'' +
                ", abhaType=" + abhaType +
                ", aBHANumber='" + aBHANumber + '\'' +
                ", abhaStatus='" + abhaStatus + '\'' +
                '}';
    }
}
