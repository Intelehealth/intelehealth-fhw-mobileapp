package org.intelehealth.ekalhelpline.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CallingPatient {

    @SerializedName("From")
    @Expose
    public String call_from;
    @SerializedName("To")
    @Expose
    public String call_to;
    @SerializedName("CallerId")
    @Expose
    public String callerId;

}
