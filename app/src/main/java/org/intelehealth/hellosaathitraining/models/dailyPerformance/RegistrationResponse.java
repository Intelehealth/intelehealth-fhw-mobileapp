package org.intelehealth.hellosaathitraining.models.dailyPerformance;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RegistrationResponse {
    @SerializedName("data")
    @Expose
    public List<Registrations> data;
}

