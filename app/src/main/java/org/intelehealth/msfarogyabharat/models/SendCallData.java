package org.intelehealth.msfarogyabharat.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SendCallData {

    @SerializedName("id")
    @Expose
    public String id;

    @SerializedName("state")
    @Expose
    public String state;

    @SerializedName("district")
    @Expose
    public String district;

    @SerializedName("facilityName")
    @Expose
    public String facility;

    @SerializedName("dateOfCalls")
    @Expose
    public String callDate;

    @SerializedName("status")
    @Expose
    public String callStatus;

    @SerializedName("actionIfCompleted")
    @Expose
    public String callAction;

    @SerializedName("callerNumber")
    @Expose
    public String callNumber;

    @SerializedName("remarks")
    @Expose
    public String remarks;

    @SerializedName("CallStartTime")
    @Expose
    public String callStartTime;

    @SerializedName("CallEndTime")
    @Expose
    public String callEndTime;

    public SendCallData(){}

    public SendCallData(String state, String district, String facility, String callDate, String callStatus, String callAction, String callNumber, String remarks, String callStartTime, String callEndTime) {
        this.state = state;
        this.district = district;
        this.facility = facility;
        this.callDate = callDate;
        this.callStatus = callStatus;
        this.callAction = callAction;
        this.callNumber = callNumber;
        this.remarks = remarks;
        this.callStartTime = callStartTime;
        this.callEndTime = callEndTime;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getFacility() {
        return facility;
    }

    public void setFacility(String facility) {
        this.facility = facility;
    }

    public String getCallDate() {
        return callDate;
    }

    public void setCallDate(String callDate) {
        this.callDate = callDate;
    }

    public String getCallStatus() {
        return callStatus;
    }

    public void setCallStatus(String callStatus) {
        this.callStatus = callStatus;
    }

    public String getCallAction() {
        return callAction;
    }

    public void setCallAction(String callAction) {
        this.callAction = callAction;
    }

    public String getCallNumber() {
        return callNumber;
    }

    public void setCallNumber(String callNumber) {
        this.callNumber = callNumber;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getCallStartTime() {
        return callStartTime;
    }

    public void setCallStartTime(String callStartTime) {
        this.callStartTime = callStartTime;
    }

    public String getCallEndTime() {
        return callEndTime;
    }

    public void setCallEndTime(String callEndTime) {
        this.callEndTime = callEndTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
