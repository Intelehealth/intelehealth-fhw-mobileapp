package org.intelehealth.vikalphelpline.activities.resolutionActivity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Resolution {
    @SerializedName("patient")
    @Expose
    public String patient;
    @SerializedName("encounterType")
    @Expose
    public String encounterType;
    @SerializedName("encounterProviders")
    @Expose
    public List<EncounterProvider> encounterProviders;
    @SerializedName("visit")
    @Expose
    public String visit;
    @SerializedName("encounterDatetime")
    @Expose
    public String encounterDatetime;
    @SerializedName("obs")
    @Expose
    public List<Obs> obs = null;

    public class EncounterProvider {
        @SerializedName("provider")
        @Expose
        public String provider;
        @SerializedName("encounterRole")
        @Expose
        public String encounterRole;
    }

    public class Obs {
        @SerializedName("concept")
        @Expose
        public String concept;
        @SerializedName("value")
        @Expose
        public String value;
    }
}
