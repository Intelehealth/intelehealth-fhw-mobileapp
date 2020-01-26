
package app.intelehealth.client.models.pushRequestApiCall;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

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

//    public List<Attribute> getAttributes() {
//        return attributes;
//    }

//    public void setAttributes(List<Attribute> attributes) {
//        this.attributes = attributes;
//    }
}
