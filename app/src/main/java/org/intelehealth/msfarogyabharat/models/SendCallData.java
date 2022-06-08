package org.intelehealth.msfarogyabharat.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SendCallData {

    @SerializedName("state")
    @Expose
    public String state;

    @SerializedName("district")
    @Expose
    public String district;

    @SerializedName("facilityName")
    @Expose
    public String facility;

    @SerializedName("dateOfCalls")
    @Expose
    public String callDate;

    @SerializedName("status")
    @Expose
    public String callStatus;

    @SerializedName("actionIfCompleted")
    @Expose
    public String callAction;

    @SerializedName("callerNumber")
    @Expose
    public String callNumber;

    @SerializedName("remarks")
    @Expose
    public String remarks;

}
