package org.intelehealth.unicef.models.prescriptionUpload;

/**
 * Created by Prajwal Maruti Waingankar on 20-12-2021, 11:23
 * Copyright (c) 2021 . All rights reserved.
 * Email: prajwalwaingankar@gmail.com
 * Github: prajwalmw
 */

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

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

}

class EncounterProvider {

    @SerializedName("provider")
    @Expose
    private String provider;
    @SerializedName("encounterRole")
    @Expose
    private String encounterRole;

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getEncounterRole() {
        return encounterRole;
    }

    public void setEncounterRole(String encounterRole) {
        this.encounterRole = encounterRole;
    }

}