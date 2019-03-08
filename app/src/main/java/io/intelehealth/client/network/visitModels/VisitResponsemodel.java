
package io.intelehealth.client.network.visitModels;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class VisitResponsemodel {
//visit response model
    @SerializedName("uuid")
    @Expose
    private String uuid;
    @SerializedName("display")
    @Expose
    private String display;
    @SerializedName("patient")
    @Expose
    private Patient patient;
    @SerializedName("visitType")
    @Expose
    private VisitType visitType;
    @SerializedName("indication")
    @Expose
    private Object indication;
    @SerializedName("location")
    @Expose
    private Location location;
    @SerializedName("startDatetime")
    @Expose
    private String startDatetime;
    @SerializedName("stopDatetime")
    @Expose
    private Object stopDatetime;
    @SerializedName("encounters")
    @Expose
    private List<Encounter> encounters = null;
    @SerializedName("attributes")
    @Expose
    private List<Object> attributes = null;
    @SerializedName("voided")
    @Expose
    private Boolean voided;
    @SerializedName("links")
    @Expose
    private List<Link____> links = null;
    @SerializedName("resourceVersion")
    @Expose

    private String resourceVersion;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public VisitType getVisitType() {
        return visitType;
    }

    public void setVisitType(VisitType visitType) {
        this.visitType = visitType;
    }

    public Object getIndication() {
        return indication;
    }

    public void setIndication(Object indication) {
        this.indication = indication;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getStartDatetime() {
        return startDatetime;
    }

    public void setStartDatetime(String startDatetime) {
        this.startDatetime = startDatetime;
    }

    public Object getStopDatetime() {
        return stopDatetime;
    }

    public void setStopDatetime(Object stopDatetime) {
        this.stopDatetime = stopDatetime;
    }

    public List<Encounter> getEncounters() {
        return encounters;
    }

    public void setEncounters(List<Encounter> encounters) {
        this.encounters = encounters;
    }

    public List<Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<Object> attributes) {
        this.attributes = attributes;
    }

    public Boolean getVoided() {
        return voided;
    }

    public void setVoided(Boolean voided) {
        this.voided = voided;
    }

    public List<Link____> getLinks() {
        return links;
    }

    public void setLinks(List<Link____> links) {
        this.links = links;
    }

    public String getResourceVersion() {
        return resourceVersion;
    }

    public void setResourceVersion(String resourceVersion) {
        this.resourceVersion = resourceVersion;
    }

}
