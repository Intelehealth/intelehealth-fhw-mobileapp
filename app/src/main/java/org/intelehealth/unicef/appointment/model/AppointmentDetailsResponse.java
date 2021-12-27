package org.intelehealth.unicef.appointment.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class AppointmentDetailsResponse implements Serializable {

    @SerializedName("status")
    private boolean status;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private AppointmentInfo data;


    public AppointmentInfo getData() {
        return data;
    }

    public void setData(AppointmentInfo data) {
        this.data = data;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
