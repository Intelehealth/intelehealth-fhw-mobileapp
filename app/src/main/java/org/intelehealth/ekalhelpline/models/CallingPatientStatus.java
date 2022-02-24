package org.intelehealth.ekalhelpline.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CallingPatientStatus {
    @SerializedName("Status")
    @Expose
    public String status;
}
