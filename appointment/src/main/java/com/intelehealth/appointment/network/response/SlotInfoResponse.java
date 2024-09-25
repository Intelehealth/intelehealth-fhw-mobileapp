package com.intelehealth.appointment.network.response;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class SlotInfoResponse implements Serializable {

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public List<SlotInfo> getDates() {
        return dates;
    }

    public void setDates(List<SlotInfo> dates) {
        this.dates = dates;
    }

    @SerializedName("status")
    private boolean status;

    @SerializedName("dates")
    private List<SlotInfo> dates;


}
