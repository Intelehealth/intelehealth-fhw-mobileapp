package org.intelehealth.kf.models.prescriptionUpload;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Prajwal Maruti Waingankar on 20-01-2022, 16:22
 * Copyright (c) 2021 . All rights reserved.
 * Email: prajwalwaingankar@gmail.com
 * Github: prajwalmw
 */

public class EndVisitEncounterPrescription {

    @SerializedName("patient")
    @Expose
    private String patient;
    @SerializedName("encounterType")
    @Expose
    private String encounterType;
    @SerializedName("encounterProviders")
    @Expose
    private List<EncounterProvider> encounterProviders = null;
    @SerializedName("visit")
    @Expose
    private String visit;
    @SerializedName("encounterDatetime")
    @Expose
    private String encounterDatetime;
    @SerializedName("obs")
    @Expose
    private List<Ob> obs = null;

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

    public List<EncounterProvider> getEncounterProviders() {
        return encounterProviders;
    }

    public void setEncounterProviders(List<EncounterProvider> encounterProviders) {
        this.encounterProviders = encounterProviders;
    }

    public String getVisit() {
        return visit;
    }

    public void setVisit(String visit) {
        this.visit = visit;
    }

    public String getEncounterDatetime() {
        return encounterDatetime;
    }

    public void setEncounterDatetime(String encounterDatetime) {
        this.encounterDatetime = encounterDatetime;
    }

    public List<Ob> getObs() {
        return obs;
    }

    public void setObs(List<Ob> obs) {
        this.obs = obs;
    }
}
