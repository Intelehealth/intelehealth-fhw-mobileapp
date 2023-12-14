
package org.intelehealth.kf.models.pushRequestApiCall;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import org.intelehealth.kf.models.dto.VisitAttribute_Speciality;

public class Visit {

    @SerializedName("uuid")
    @Expose
    private String uuid;
    @SerializedName("startDatetime")
    @Expose
    private String startDatetime;
    @SerializedName("stopDatetime")
    @Expose
    private String stopDatetime;
    @SerializedName("visitType")
    @Expose
    private String visitType;
    @SerializedName("patient")
    @Expose
    private String patient;
    @SerializedName("location")
    @Expose
    private String location;
    @SerializedName("attributes")
    @Expose
    private List<VisitAttribute_Speciality> attributes = null;
//    @SerializedName("attributes")
//    @Expose
//    private List<Attribute> attributes = null;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getStartDatetime() {
        return startDatetime;
    }

    public void setStartDatetime(String startDatetime) {
        this.startDatetime = startDatetime;
    }

    public String getVisitType() {
        return visitType;
    }

    public void setVisitType(String visitType) {
        this.visitType = visitType;
    }

    public String getPatient() {
        return patient;
    }

    public void setPatient(String patient) {
        this.patient = patient;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getStopDatetime() {
        return stopDatetime;
    }

    public void setStopDatetime(String stopDatetime) {
        this.stopDatetime = stopDatetime;
    }

    public List<VisitAttribute_Speciality> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<VisitAttribute_Speciality> attributes) {
        this.attributes = attributes;
    }

//    public List<Attribute> getAttributes() {
//        return attributes;
//    }
//
//    public void setAttributes(List<Attribute> attributes) {
//        this.attributes = attributes;
//    }
}
