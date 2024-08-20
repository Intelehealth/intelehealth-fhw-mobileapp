
package org.intelehealth.vikalphelpline.models.ObsImageModel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ObsPushDTO {

    @SerializedName("uuid")
    @Expose
    private String uuid;
    @SerializedName("concept")
    @Expose
    private String concept;
    @SerializedName("encounter")
    @Expose
    private String encounter;
    @SerializedName("obsDatetime")
    @Expose
    private String obsDatetime;
    @SerializedName("person")
    @Expose
    private String person;
    @SerializedName("value")
    @Expose
    private String value;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getConcept() {
        return concept;
    }

    public void setConcept(String concept) {
        this.concept = concept;
    }

    public String getEncounter() {
        return encounter;
    }

    public void setEncounter(String encounter) {
        this.encounter = encounter;
    }

    public String getObsDatetime() {
        return obsDatetime;
    }

    public void setObsDatetime(String obsDatetime) {
        this.obsDatetime = obsDatetime;
    }

    public String getPerson() {
        return person;
    }

    public void setPerson(String person) {
        this.person = person;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
