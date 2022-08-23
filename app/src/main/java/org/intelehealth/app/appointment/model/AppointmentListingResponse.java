package org.intelehealth.app.appointment.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class AppointmentListingResponse implements Serializable {

    @SerializedName("status")
    private boolean status;

    @SerializedName("data")
    private List<AppointmentInfo> data;



    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public List<AppointmentInfo> getData() {
        return data;
    }

    public void setData(List<AppointmentInfo> data) {
        this.data = data;
    }
}
