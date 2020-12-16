
package app.intelehealth.client.models.uploadSurvey;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Survey {

    @SerializedName("encounterDatetime")
    @Expose
    private String encounterDatetime;
    @SerializedName("patient")
    @Expose
    private String patient;
    @SerializedName("encounterType")
    @Expose
    private String encounterType;
    @SerializedName("visit")
    @Expose
    private String visit;
    @SerializedName("obs")
    @Expose
    private List<Ob> obs = null;
    @SerializedName("encounterProviders")
    @Expose
    private List<EncounterProvider> encounterProviders = null;
    @SerializedName("location")
    @Expose
    private String location;

    public String getEncounterDatetime() {
        return encounterDatetime;
    }

    public void setEncounterDatetime(String encounterDatetime) {
        this.encounterDatetime = encounterDatetime;
    }

    public String getPatient() {
        return patient;
    }

    public void setPatient(String patient) {
        this.patient = patient;
    }

    public String getEncounterType() {
        return encounterType;
    }

    public void setEncounterType(String encounterType) {
        this.encounterType = encounterType;
    }

    public String getVisit() {
        return visit;
    }

    public void setVisit(String visit) {
        this.visit = visit;
    }

    public List<Ob> getObs() {
        return obs;
    }

    public void setObs(List<Ob> obs) {
        this.obs = obs;
    }

    public List<EncounterProvider> getEncounterProviders() {
        return encounterProviders;
    }

    public void setEncounterProviders(List<EncounterProvider> encounterProviders) {
        this.encounterProviders = encounterProviders;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

}
