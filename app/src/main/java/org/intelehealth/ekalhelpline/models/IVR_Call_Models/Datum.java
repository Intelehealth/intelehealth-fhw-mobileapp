package org.intelehealth.ekalhelpline.models.IVR_Call_Models;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created By: Prajwal Waingankar on 19-Aug-21
 * Github: prajwalmw
 * Email: prajwalwaingankar@gmail.com
 */

public class Datum {

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("uniqid")
    @Expose
    private String uniqid;
    @SerializedName("parent_id")
    @Expose
    private String parentId;
    @SerializedName("callfrom")
    @Expose
    private String callfrom;
    @SerializedName("callto")
    @Expose
    private String callto;
    @SerializedName("start_time")
    @Expose
    private String startTime;
    @SerializedName("end_time")
    @Expose
    private String endTime;
    @SerializedName("duration")
    @Expose
    private Integer duration;
    @SerializedName("billsec")
    @Expose
    private String billsec;
    @SerializedName("notes")
    @Expose
    private String notes;
    @SerializedName("location")
    @Expose
    private String location;
    @SerializedName("provider")
    @Expose
    private String provider;
    @SerializedName("credits")
    @Expose
    private String credits;
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("recording")
    @Expose
    private String recording;
    @SerializedName("date")
    @Expose
    private String date;
    @SerializedName("caller_id")
    @Expose
    private String callerId;
    @SerializedName("service")
    @Expose
    private String service;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUniqid() {
        return uniqid;
    }

    public void setUniqid(String uniqid) {
        this.uniqid = uniqid;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getCallfrom() {
        return callfrom;
    }

    public void setCallfrom(String callfrom) {
        this.callfrom = callfrom;
    }

    public String getCallto() {
        return callto;
    }

    public void setCallto(String callto) {
        this.callto = callto;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getBillsec() {
        return billsec;
    }

    public void setBillsec(String billsec) {
        this.billsec = billsec;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getCredits() {
        return credits;
    }

    public void setCredits(String credits) {
        this.credits = credits;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRecording() {
        return recording;
    }

    public void setRecording(String recording) {
        this.recording = recording;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCallerId() {
        return callerId;
    }

    public void setCallerId(String callerId) {
        this.callerId = callerId;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

}

