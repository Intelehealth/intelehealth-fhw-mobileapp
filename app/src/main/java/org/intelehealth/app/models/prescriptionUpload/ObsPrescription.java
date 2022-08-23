package org.intelehealth.app.models.prescriptionUpload;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Prajwal Maruti Waingankar on 20-01-2022, 16:27
 * Copyright (c) 2021 . All rights reserved.
 * Email: prajwalwaingankar@gmail.com
 * Github: prajwalmw
 */

public class ObsPrescription {

    @SerializedName("concept")
    @Expose
    private String concept;
    @SerializedName("person")
    @Expose
    private String person;
    @SerializedName("obsDatetime")
    @Expose
    private String obsDatetime;
    @SerializedName("value")
    @Expose
    private String value;
    @SerializedName("encounter")
    @Expose
    private String encounter;

    public String getConcept() {
        return concept;
    }

    public void setConcept(String concept) {
        this.concept = concept;
    }

    public String getPerson() {
        return person;
    }

    public void setPerson(String person) {
        this.person = person;
    }

    public String getObsDatetime() {
        return obsDatetime;
    }

    public void setObsDatetime(String obsDatetime) {
        this.obsDatetime = obsDatetime;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getEncounter() {
        return encounter;
    }

    public void setEncounter(String encounter) {
        this.encounter = encounter;
    }

}
