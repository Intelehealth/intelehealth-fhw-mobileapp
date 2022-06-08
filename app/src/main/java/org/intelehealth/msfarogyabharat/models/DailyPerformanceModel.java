package org.intelehealth.msfarogyabharat.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DailyPerformanceModel {
    @SerializedName("data")
    @Expose
    private List<Datum> data = null;
    @SerializedName("status")
    @Expose
    private String status;

    public List<Datum> getData() { return data; }

    public void setData(List<Datum> data) { this.data = data; }

    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status; }

    public class Datum {

        @SerializedName("Count")
        @Expose
        private Integer count;
        @SerializedName("actionIfCompleted")
        @Expose
        private String actionIfCompleted;
        @SerializedName("dateOfCalls")
        @Expose
        private String dateOfCalls;
        @SerializedName("status")
        @Expose
        private String status;

        public Integer getCount() { return count; }

        public void setCount(Integer count) { this.count = count; }

        public String getActionIfCompleted() { return actionIfCompleted; }

        public void setActionIfCompleted(String actionIfCompleted) { this.actionIfCompleted = actionIfCompleted; }

        public String getDateOfCalls() { return dateOfCalls; }

        public void setDateOfCalls(String dateOfCalls) { this.dateOfCalls = dateOfCalls; }

        public String getStatus() { return status; }

        public void setStatus(String status) { this.status = status; }
    }


}
