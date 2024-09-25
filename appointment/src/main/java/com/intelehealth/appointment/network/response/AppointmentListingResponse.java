package com.intelehealth.appointment.network.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class AppointmentListingResponse implements Serializable {

    @SerializedName("status")
    @Expose
    private boolean status;
    @SerializedName("data")
    @Expose
    private List<AppointmentInfo> data = null;
    @SerializedName("cancelledAppointments")
    @Expose
    private List<AppointmentInfo> cancelledAppointments = null;

    public List<AppointmentInfo> getData() {
        return data;
    }

    public void setData(List<AppointmentInfo> data) {
        this.data = data;
    }

    public List<AppointmentInfo> getCancelledAppointments() {
        return cancelledAppointments;
    }

    public void setCancelledAppointments(List<AppointmentInfo> cancelledAppointments) {
        this.cancelledAppointments = cancelledAppointments;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

}
